// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.intention.impl.config;

import com.intellij.DynamicBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.util.text.Strings;
import com.intellij.util.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static com.intellij.DynamicBundle.findLanguageBundle;

final class ResourceTextDescriptor implements TextDescriptor {
  private static final Logger LOG = Logger.getInstance(ResourceTextDescriptor.class);
  private final ClassLoader myLoader;
  private final String myResourcePath;

  ResourceTextDescriptor(ClassLoader loader, @NotNull String resourcePath) {
    myLoader = loader;
    myResourcePath = resourcePath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ResourceTextDescriptor resource = (ResourceTextDescriptor)o;
    return Objects.equals(myLoader, resource.myLoader) &&
           Objects.equals(myResourcePath, resource.myResourcePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myLoader, myResourcePath);
  }

  @NotNull
  @Override
  public String getText() throws IOException {
    InputStream stream = myLoader.getResourceAsStream(myResourcePath);
    if (stream == null) throw new IOException("Resource not found: " + myResourcePath);

    InputStream languageStream = getLanguageStream();
    if (languageStream == null || !myResourcePath.endsWith(BeforeAfterActionMetaData.DESCRIPTION_FILE_NAME)) {
      return ResourceUtil.loadText(stream); //NON-NLS;
    }

    try {
      return ResourceUtil.loadText(languageStream); //NON-NLS
    }
    catch (IOException e) {
      LOG.error("Cannot find localized resource: " + myResourcePath, e);
      return ResourceUtil.loadText(stream); //NON-NLS
    }
  }

  @Nullable
  private InputStream getLanguageStream() {
    DynamicBundle.LanguageBundleEP langBundle = findLanguageBundle();
    if (langBundle == null) return null;

    PluginDescriptor descriptor = langBundle.pluginDescriptor;
    if (descriptor == null) return null;

    ClassLoader classLoader = descriptor.getPluginClassLoader();
    return classLoader != null ? classLoader.getResourceAsStream(myResourcePath) : null;
  }

  @NotNull
  @Override
  public String getFileName() {
    return Strings.trimEnd(myResourcePath.substring(myResourcePath.lastIndexOf('/') + 1), BeforeAfterActionMetaData.EXAMPLE_USAGE_URL_SUFFIX);
  }
}