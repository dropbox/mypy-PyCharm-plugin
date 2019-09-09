package com.dropbox.plugins.mypy_plugin.actions;

import com.dropbox.plugins.mypy_plugin.MypyConfigLoader;
import com.dropbox.plugins.mypy_plugin.model.MypyConfig;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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
        String path = vf.getPath();

        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null)
            return;
        String directory = project.getBaseDir().getPath();
        MypyConfig config = MypyConfigLoader.findMypyConfig(project);

        Process process;
        ProcessBuilder processBuilder = new ProcessBuilder();
        Map<String, String> envProcess = processBuilder.environment();
        Map<String, String> env = System.getenv();

        envProcess.putAll(env);
        String extraPath = config.getPathSuffix();
        if (!extraPath.equals("")) {
            envProcess.put("PATH", envProcess.get("PATH") + File.pathSeparator + extraPath);
        }
        processBuilder.command("/bin/bash", "-c", "./mypy-suggest " + path + " " +
                String.valueOf(line + 1));
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectInput(new File("/dev/null"));
        try {
            process = processBuilder.directory(new File(directory)).start();
            process.waitFor();
        } catch (IOException | InterruptedException ex) {
            ApplicationManager.getApplication().invokeLater(() -> Messages.showMessageDialog(project, ex.getMessage(),
                    "Plugin Exception:", Messages.getErrorIcon()));
            return;
        }
        vf.refresh(true, false);
    }
}
