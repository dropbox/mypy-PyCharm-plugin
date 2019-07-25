package com.dropbox.plugins.mypy_plugin;

import com.dropbox.plugins.mypy_plugin.model.MypyConfig;
import com.dropbox.plugins.mypy_plugin.model.MypyError;
import com.dropbox.plugins.mypy_plugin.model.MypyResult;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public final class MypyRunner {
    private final JList<MypyError> display;
    private final Project project;
    private boolean isRunning;

    MypyRunner(JList<MypyError> display, Project project) {
        this.display = display;
        this.project = project;
        this.isRunning = false;
    }

    @Nullable
    MypyResult runMypyDaemon() {
        Process process;
        String directory = project.getBaseDir().getPath();
        MypyConfig config = MypyConfigLoader.findMypyConfig(project);

        ProcessBuilder processBuilder = new ProcessBuilder();
        Map<String, String> envProcess = processBuilder.environment();
        Map<String, String> env = System.getenv();

        envProcess.putAll(env);
        String extraPath = config.getPathSuffix();
        if (!extraPath.equals("")) {
            envProcess.put("PATH", envProcess.get("PATH") + File.pathSeparator + extraPath);
        }
        String mypyCommand = config.getExecutableName();
        processBuilder.command("/bin/bash", "-c", mypyCommand);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectInput(new File("/dev/null"));
        this.isRunning = true;
        try {
            process = processBuilder.directory(new File(directory)).start();
        } catch (IOException e) {
            ApplicationManager.getApplication().invokeLater(() -> Messages.showMessageDialog(project, e.getMessage(),
                    "Plugin Exception:", Messages.getErrorIcon()));
            this.isRunning = false;
            return null;
        }
        ArrayList<MypyError> lines = new ArrayList<>();
        ArrayList<MypyError> debug = new ArrayList<>();
        BufferedReader br=new BufferedReader(
                new InputStreamReader(
                        process.getInputStream()));
        MypyError[] data;
        int errCount = 0;
        int noteCount = 0;
        try {
            String line;
            while((line=br.readLine()) != null) {
                if (line.matches(MypyTerminal.ERROR_RE) | line.matches(MypyTerminal.NOTE_RE)) {
                    lines.add(new MypyError(line, line.matches(MypyTerminal.ERROR_RE) ? MypyError.ERROR : MypyError.NOTE));
                    if (line.matches(MypyTerminal.ERROR_RE)) {
                        errCount++;
                    }
                    if (line.matches(MypyTerminal.NOTE_RE)) {
                        noteCount++;
                    }
                } else if (line.matches("PASSED") | line.matches("FAILED")) {
                    // these will be shown in status line anyway
                    break;
                } else {
                    debug.add(new MypyError(line, MypyError.DEBUG));
                }
                data = new MypyError[debug.size()];
                data = debug.toArray(data);
                this.display.setListData(data);
                int max = this.display.getModel().getSize();
                if (max > 0) {
                    this.display.scrollRectToVisible(this.display.getCellBounds(max - 1, max));
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            ApplicationManager.getApplication().invokeLater(() -> Messages.showMessageDialog(project, e.getMessage(),
                    "Plugin Exception:", Messages.getErrorIcon()));
            this.isRunning = false;
            return null;
        }
        lines.sort(Comparator.comparing(MypyError::getFile));
        this.isRunning = false;
        return new MypyResult(process.exitValue(), errCount, noteCount, lines);
    }

    public boolean isRunning() {
        return isRunning;
    }
}
