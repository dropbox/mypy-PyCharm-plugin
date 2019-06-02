package com.dropbox.plugins.mypy_plugin;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;


public final class MypyToolWindowFactory implements ToolWindowFactory, DumbAware {
    final static String DEFAULT_MYPY_PATH_SUFFIX = "";
    final static String DEFAULT_MYPY_COMMAND = "dmypy start -- --follow-imports=error ; dmypy check .";
    final public static String MYPY_PLUGIN_ID = "Mypy Terminal";
    final public static boolean DEBUG_BUILD = false;
    private final static HashMap<Project, MypyTerminal> instances = new HashMap<>();

    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MypyTerminal terminal = new MypyTerminal(project);
        terminal.initUI(toolWindow);
        instances.put(project, terminal);
    }

    public static MypyTerminal getMypyTerminal(Project project) {
        return instances.get(project);
    }
}
