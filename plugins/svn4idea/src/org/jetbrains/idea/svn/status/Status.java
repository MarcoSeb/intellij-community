/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.svn.status;

import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.api.NodeKind;
import org.jetbrains.idea.svn.api.Revision;
import org.jetbrains.idea.svn.conflict.TreeConflictDescription;
import org.jetbrains.idea.svn.lock.Lock;
import org.tmatesoft.svn.core.SVNURL;

import java.io.File;

/**
 * TODO: Could also inherit BaseNodeDescription when myKind becomes final.
 */
public class Status {
  private SVNURL myURL;
  private File myFile;
  private @NotNull NodeKind myKind;
  @NotNull private Revision myRevision;
  @NotNull private Revision myCommittedRevision;
  private StatusType myContentsStatus;
  private StatusType myPropertiesStatus;
  private StatusType myRemoteContentsStatus;
  private StatusType myRemoteNodeStatus;
  private StatusType myRemotePropertiesStatus;
  private boolean myIsLocked;
  private boolean myIsCopied;
  private boolean myIsSwitched;
  private SVNURL myCopyFromURL;
  @Nullable private Lock myRemoteLock;
  @Nullable private Lock myLocalLock;
  private Revision myRemoteRevision;
  private String myChangelistName;
  @Nullable private TreeConflictDescription myTreeConflict;
  private boolean myIsConflicted;

  private StatusType myNodeStatus;
  private SVNURL myRepositoryRootURL;

  public Status(SVNURL url,
                File file,
                @NotNull NodeKind kind,
                @Nullable Revision revision,
                @Nullable Revision committedRevision,
                StatusType contentsStatus,
                StatusType propertiesStatus,
                StatusType remoteContentsStatus,
                StatusType remotePropertiesStatus,
                boolean isLocked,
                boolean isCopied,
                boolean isSwitched,
                SVNURL copyFromURL,
                @Nullable Lock remoteLock,
                @Nullable Lock localLock,
                String changelistName,
                @Nullable TreeConflictDescription treeConflict) {
    myURL = url;
    myFile = file;
    myKind = kind;
    myRevision = revision == null ? Revision.UNDEFINED : revision;
    myCommittedRevision = committedRevision == null ? Revision.UNDEFINED : committedRevision;
    myContentsStatus = contentsStatus == null ? StatusType.STATUS_NONE : contentsStatus;
    myPropertiesStatus = propertiesStatus == null ? StatusType.STATUS_NONE : propertiesStatus;
    myRemoteContentsStatus = remoteContentsStatus == null ? StatusType.STATUS_NONE : remoteContentsStatus;
    myRemotePropertiesStatus = remotePropertiesStatus == null ? StatusType.STATUS_NONE : remotePropertiesStatus;
    myRemoteNodeStatus = StatusType.STATUS_NONE;
    myIsLocked = isLocked;
    myIsCopied = isCopied;
    myIsSwitched = isSwitched;
    myCopyFromURL = copyFromURL;
    myRemoteLock = remoteLock;
    myLocalLock = localLock;
    myChangelistName = changelistName;
    myTreeConflict = treeConflict;
    myRemoteRevision = Revision.UNDEFINED;
  }

  public Status() {
    setRevision(Revision.UNDEFINED);
    myRemoteRevision = Revision.UNDEFINED;
  }

  public SVNURL getURL() {
    return myURL;
  }

  public File getFile() {
    return myFile;
  }

  @NotNull
  public NodeKind getKind() {
    return myKind;
  }

  @NotNull
  public Revision getRevision() {
    return myRevision;
  }

  @NotNull
  public Revision getCommittedRevision() {
    return myCommittedRevision;
  }

  public StatusType getContentsStatus() {
    return myContentsStatus;
  }

  public StatusType getPropertiesStatus() {
    return myPropertiesStatus;
  }

  public StatusType getRemoteContentsStatus() {
    return myRemoteContentsStatus;
  }

  public StatusType getRemotePropertiesStatus() {
    return myRemotePropertiesStatus;
  }

  public boolean is(@NotNull StatusType type) {
    return type.equals(getNodeStatus()) || type.equals(getContentsStatus());
  }

  public boolean is(@NotNull StatusType... types) {
    return ContainerUtil.or(types, type -> is(type));
  }

  public boolean isProperty(@NotNull StatusType type) {
    return type.equals(getPropertiesStatus());
  }

  public boolean isProperty(@NotNull StatusType... types) {
    return ContainerUtil.or(types, type -> isProperty(type));
  }

  public boolean isLocked() {
    return myIsLocked;
  }

  public boolean isCopied() {
    return myIsCopied;
  }

  public boolean isSwitched() {
    return myIsSwitched;
  }

  @Nullable
  public SVNURL getCopyFromURL() {
    return myCopyFromURL;
  }

  @Nullable
  public Lock getRemoteLock() {
    return myRemoteLock;
  }

  @Nullable
  public Lock getLocalLock() {
    return myLocalLock;
  }

  public Revision getRemoteRevision() {
    return myRemoteRevision;
  }

  public String getChangelistName() {
    return myChangelistName;
  }

  @Nullable
  public TreeConflictDescription getTreeConflict() {
    return myTreeConflict;
  }

  public boolean isConflicted() {
    return myIsConflicted;
  }

  public StatusType getRemoteNodeStatus() {
    return myRemoteNodeStatus;
  }

  public StatusType getNodeStatus() {
    if (myNodeStatus == null) {
      return myContentsStatus;
    }
    return myNodeStatus;
  }

  public SVNURL getRepositoryRootURL() {
    return myRepositoryRootURL;
  }

  public void setURL(SVNURL uRL) {
    myURL = uRL;
  }

  public void setFile(File file) {
    myFile = file;
  }

  public void setKind(@NotNull NodeKind kind) {
    myKind = kind;
  }

  public void setRevision(@NotNull Revision revision) {
    myRevision = revision;
  }

  public void setCommittedRevision(@NotNull Revision committedRevision) {
    myCommittedRevision = committedRevision;
  }

  public void setContentsStatus(StatusType statusType) {
    myContentsStatus = statusType;
  }

  public void setPropertiesStatus(StatusType propertiesStatus) {
    myPropertiesStatus = propertiesStatus;
  }

  public void setRemoteContentsStatus(StatusType remoteContentsStatus) {
    myRemoteContentsStatus = remoteContentsStatus;
  }

  public void setRemotePropertiesStatus(StatusType remotePropertiesStatus) {
    myRemotePropertiesStatus = remotePropertiesStatus;
  }

  public void setIsLocked(boolean isLocked) {
    myIsLocked = isLocked;
  }

  public void setIsCopied(boolean isCopied) {
    myIsCopied = isCopied;
  }

  public void setIsSwitched(boolean isSwitched) {
    myIsSwitched = isSwitched;
  }

  public void setRemoteLock(@Nullable Lock remoteLock) {
    myRemoteLock = remoteLock;
  }

  public void setLocalLock(@Nullable Lock localLock) {
    myLocalLock = localLock;
  }

  public void setChangelistName(String changelistName) {
    myChangelistName = changelistName;
  }

  public void setIsConflicted(boolean isConflicted) {
    myIsConflicted = isConflicted;
  }

  public void setRemoteNodeStatus(StatusType remoteNodeStatus) {
    myRemoteNodeStatus = remoteNodeStatus;
  }

  public void setNodeStatus(StatusType nodeStatus) {
    myNodeStatus = nodeStatus;
  }

  public void setRepositoryRootURL(SVNURL repositoryRootURL) {
    myRepositoryRootURL = repositoryRootURL;
  }

  public void setRemoteRevision(Revision remoteRevision) {
    myRemoteRevision = remoteRevision;
  }
}
