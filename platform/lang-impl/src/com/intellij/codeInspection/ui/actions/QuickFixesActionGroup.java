/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package com.intellij.codeInspection.ui.actions;

import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.codeInspection.ex.QuickFixAction;
import com.intellij.codeInspection.ui.InspectionResultsView;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.codeInspection.ui.actions.InspectionViewActionBase.getView;

/**
 * @author Dmitry Batkovich
 */
public class QuickFixesActionGroup extends ActionGroup {
  @NotNull
  @Override
  public AnAction[] getChildren(@Nullable AnActionEvent e) {
    final InspectionResultsView view = getView(e);
    if (view == null) {
      return AnAction.EMPTY_ARRAY;
    }
    final InspectionToolWrapper wrapper = view.getTree().getSelectedToolWrapper();
    if (wrapper == null) return AnAction.EMPTY_ARRAY;

    final QuickFixAction[] fixes = view.getProvider().getQuickFixes(wrapper, view.getTree());
    return fixes == null ? AnAction.EMPTY_ARRAY : fixes;
  }
}
