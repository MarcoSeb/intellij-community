// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.idea.maven.server.wsl

import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.wsl.WSLDistribution
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import org.jetbrains.idea.maven.server.AbstractMavenServerRemoteProcessSupport
import org.jetbrains.idea.maven.server.WslMavenDistribution

internal class WslMavenServerRemoteProcessSupport(private val myWslDistribution: WSLDistribution,
                                                  jdk: Sdk,
                                                  vmOptions: String?,
                                                  mavenDistribution: WslMavenDistribution,
                                                  project: Project,
                                                  debugPort: Int?,
                                                  multimoduleDirectory: String?)
  : AbstractMavenServerRemoteProcessSupport(jdk, vmOptions, mavenDistribution, project, debugPort, multimoduleDirectory) {
  override fun getRunProfileState(target: Any, configuration: Any, executor: Executor): RunProfileState {
    return WslMavenCmdState(myWslDistribution, myJdk, myOptions, myDistribution as WslMavenDistribution,
                            myDebugPort, myProject, remoteHost, myMultimoduleDirectory)
  }

  override fun getRemoteHost(): String = myWslDistribution.wslIp

  override fun type() = "WSL"
}


