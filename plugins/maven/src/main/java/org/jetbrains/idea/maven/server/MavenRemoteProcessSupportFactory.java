// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.idea.maven.server;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.rmi.RemoteProcessSupport;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface MavenRemoteProcessSupportFactory {
  ExtensionPointName<MavenRemoteProcessSupportFactory> MAVEN_SERVER_SUPPORT_EP_NAME = new ExtensionPointName<>("org.jetbrains.idea.maven.mavenServerSupportFactory");

  MavenRemoteProcessSupport create(Sdk jdk,
                                   String vmOptions,
                                   MavenDistribution distribution,
                                   Project project,
                                   Integer debugPort,
                                   String baseMultimoduleDirectory);

  @NotNull
  static MavenRemoteProcessSupportFactory forProject(@NotNull Project project) {
    MavenRemoteProcessSupportFactory applicable = MAVEN_SERVER_SUPPORT_EP_NAME.findFirstSafe(factory -> factory.isApplicable(project));
    if (applicable == null) {
      return new LocalMavenRemoteProcessSupportFactory();
    }
    return applicable;
  }

  boolean isApplicable(Project project);

  abstract class MavenRemoteProcessSupport extends RemoteProcessSupport<Object, MavenServer, Object>{
    public MavenRemoteProcessSupport(@NotNull Class<MavenServer> valueClass) {
      super(valueClass);
    }
    public abstract String type();

    public abstract void onTerminate(Consumer<ProcessEvent> onTerminate);
  }
}
