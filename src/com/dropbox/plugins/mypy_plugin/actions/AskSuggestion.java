package com.dropbox.plugins.mypy_plugin.actions;

import com.dropbox.plugins.mypy_plugin.MypyTerminal;
import com.dropbox.plugins.mypy_plugin.MypyToolWindowFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;


final class AskSuggestion extends AnAction implements DumbAware {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null)
            return;
        LogicalPosition pos = editor.getCaretModel().getPrimaryCaret().getLogicalPosition();
        int line = pos.line;
        FileDocumentManager.getInstance().saveAllDocuments();
        VirtualFile vf = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (vf == null)
            return;
        String command = "./mypy-suggest " + vf.getPath() + " " + String.valueOf(line + 1);

        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        ToolWindow tw = ToolWindowManager.getInstance(project).getToolWindow(
                MypyToolWindowFactory.MYPY_PLUGIN_ID);
        if (!tw.isVisible()) {
            tw.show(null);
        }
        MypyTerminal terminal = MypyToolWindowFactory.getMypyTerminal(project);
        if (terminal == null) {
            return;
        }
        if (terminal.getRunner().isRunning()) {
            return;
        }
        terminal.runMypyDaemonUIWrapper(command, vf);
        vf.refresh(false, false);
    }
}
