package org.jetbrains.io.fastCgi

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.ByteBufUtilEx
import io.netty.channel.Channel
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import org.jetbrains.builtInWebServer.WebServerPathToFileManager
import org.jetbrains.io.Responses
import java.net.InetSocketAddress
import java.util.Locale

class FastCgiRequest(val requestId: Int, allocator: ByteBufAllocator) {
  companion object {
    private val PARAMS = 4
    private val BEGIN_REQUEST = 1
    private val RESPONDER = 1
    private val FCGI_KEEP_CONNECTION = 1
    private val STDIN = 5
    private val VERSION = 1
  }

  private var buffer: ByteBuf? = allocator.ioBuffer(4096)

  init {
    writeHeader(buffer!!, BEGIN_REQUEST, FastCgiConstants.HEADER_LENGTH)
    buffer!!.writeShort(RESPONDER)
    buffer!!.writeByte(FCGI_KEEP_CONNECTION)
    // reserved[5]
    buffer!!.writeZero(5)
  }

  public fun writeFileHeaders(file: VirtualFile, project: Project, canonicalRequestPath: CharSequence) {
    val root = WebServerPathToFileManager.getInstance(project).getRoot(file)
    LOG.assertTrue(root != null)
    addHeader("DOCUMENT_ROOT", root!!.getRoot().getPath())
    addHeader("SCRIPT_FILENAME", file.getPath())
    addHeader("SCRIPT_NAME", canonicalRequestPath)
  }

  public fun addHeader(key: String, value: CharSequence?) {
    if (value == null) {
      return
    }

    val keyLength = key.length()
    val valLength = value.length()
    writeHeader(buffer!!, PARAMS, keyLength + valLength + (if (keyLength < 128) 1 else 4) + (if (valLength < 128) 1 else 4))

    if (keyLength < 128) {
      buffer!!.writeByte(keyLength)
    }
    else {
      buffer!!.writeByte(128 or (keyLength shr 24))
      buffer!!.writeByte(keyLength shr 16)
      buffer!!.writeByte(keyLength shr 8)
      buffer!!.writeByte(keyLength)
    }

    if (valLength < 128) {
      buffer!!.writeByte(valLength)
    }
    else {
      buffer!!.writeByte(128 or (valLength shr 24))
      buffer!!.writeByte(valLength shr 16)
      buffer!!.writeByte(valLength shr 8)
      buffer!!.writeByte(valLength)
    }

    ByteBufUtil.writeAscii(buffer, key)
    ByteBufUtilEx.writeUtf8(buffer, value)
  }

  public fun writeHeaders(request: FullHttpRequest, clientChannel: Channel) {
    addHeader("REQUEST_URI", request.uri())
    addHeader("REQUEST_METHOD", request.method().name())

    val remote = clientChannel.remoteAddress() as InetSocketAddress
    addHeader("REMOTE_ADDR", remote.getAddress().getHostAddress())
    addHeader("REMOTE_PORT", Integer.toString(remote.getPort()))

    val local = clientChannel.localAddress() as InetSocketAddress
    addHeader("SERVER_SOFTWARE", Responses.getServerHeaderValue())
    addHeader("SERVER_NAME", Responses.getServerHeaderValue())

    addHeader("SERVER_ADDR", local.getAddress().getHostAddress())
    addHeader("SERVER_PORT", Integer.toString(local.getPort()))

    addHeader("GATEWAY_INTERFACE", "CGI/1.1")
    addHeader("SERVER_PROTOCOL", request.protocolVersion().text())
    addHeader("CONTENT_TYPE", request.headers().get(HttpHeaderNames.CONTENT_TYPE))

    // PHP only, required if PHP was built with --enable-force-cgi-redirect
    addHeader("REDIRECT_STATUS", "200")

    var queryString = ""
    val queryIndex = request.uri().indexOf('?')
    if (queryIndex != -1) {
      queryString = request.uri().substring(queryIndex + 1)
    }
    addHeader("QUERY_STRING", queryString)

    addHeader("CONTENT_LENGTH", request.content().readableBytes().toString())

    for (entry in request.headers()) {
      addHeader("HTTP_${entry.getKey().replace('-', '_').toUpperCase(Locale.ENGLISH)}", entry.getValue())
    }
  }

  fun writeToServerChannel(content: ByteBuf?, fastCgiChannel: Channel) {
    if (fastCgiChannel.pipeline().first() == null) {
      throw IllegalStateException("No handler in the pipeline")
    }

    var releaseContent = content != null
    try {
      writeHeader(buffer!!, PARAMS, 0)

      if (content != null) {
        writeHeader(buffer!!, STDIN, content.readableBytes())
      }

      fastCgiChannel.write(buffer)
      buffer = null

      if (content != null) {
        fastCgiChannel.write(content)
        // channel.write releases
        releaseContent = false

        val headerBuffer = fastCgiChannel.alloc().ioBuffer(FastCgiConstants.HEADER_LENGTH, FastCgiConstants.HEADER_LENGTH)
        writeHeader(headerBuffer, STDIN, 0)
        fastCgiChannel.write(headerBuffer)
      }
    }
    finally {
      if (releaseContent) {
        assert(content != null)
        content!!.release()
      }
    }

    fastCgiChannel.flush()
  }

  private fun writeHeader(buffer: ByteBuf, type: Int, length: Int) {
    buffer.writeByte(VERSION)
    buffer.writeByte(type)
    buffer.writeShort(requestId)
    buffer.writeShort(length)
    // paddingLength, reserved
    buffer.writeZero(2)
  }
}