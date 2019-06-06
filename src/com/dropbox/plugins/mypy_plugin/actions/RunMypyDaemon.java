package com.dropbox.plugins.mypy_plugin.actions;

import com.dropbox.plugins.mypy_plugin.MypyTerminal;
import com.dropbox.plugins.mypy_plugin.MypyToolWindowFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

final class RunMypyDaemon extends AnAction implements DumbAware {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
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
        terminal.runMypyDaemonUIWrapper();
    }
}
