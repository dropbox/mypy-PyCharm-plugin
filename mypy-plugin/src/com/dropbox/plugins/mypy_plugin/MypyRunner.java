package com.dropbox.plugins.mypy_plugin;

import com.dropbox.plugins.mypy_plugin.model.MypyError;
import com.dropbox.plugins.mypy_plugin.model.MypyResult;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class MypyRunner {

    private JList<MypyError> display;
    private Project project;
    private boolean isRunning;

    public MypyRunner(JList<MypyError> display, Project project) {
        this.display = display;
        this.project = project;
        this.isRunning = false;
    }

    public MypyResult runMypyDaemon() {
        Process process;
        String directory = project.getBaseDir().getPath();
        MypyConfigService mConfig = MypyConfigService.getInstance(project);

        ProcessBuilder pbuilder = new ProcessBuilder();
        Map<String, String> envProcess = pbuilder.environment();
        Map<String, String> env = System.getenv();

        envProcess.putAll(env);
        String extraPath = mConfig.getPathSuffix();
        if (extraPath == null) {  // config deleted
            extraPath = MypyToolWindowFactory.DEFAULT_MYPY_PATH_SUFFIX;
        }
        if (!extraPath.equals("")) {
            envProcess.put("PATH", envProcess.get("PATH") + File.pathSeparator + extraPath);
        }
        String mypyCommand = mConfig.getExecutableName();
        if ((mypyCommand == null) || (mypyCommand.equals(""))) {
            mypyCommand = MypyToolWindowFactory.DEFAULT_MYPY_COMMAND;
        }
        pbuilder.command("/bin/bash", "-c", mypyCommand);
        pbuilder.redirectErrorStream(true);
        pbuilder.redirectInput(new File("/dev/null"));
        this.isRunning = true;
        try {
            process = pbuilder.directory(new File(directory)).start();
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
        int errcount = 0;
        int notecount = 0;
        try {
            String line;
            while((line=br.readLine()) != null) {
                if (line.matches(MypyTerminal.ERROR_RE) | line.matches(MypyTerminal.NOTE_RE)) {
                    lines.add(new MypyError(line, line.matches(MypyTerminal.ERROR_RE) ? MypyError.ERROR : MypyError.NOTE));
                    if (line.matches(MypyTerminal.ERROR_RE)) {
                        errcount++;
                    }
                    if (line.matches(MypyTerminal.NOTE_RE)) {
                        notecount++;
                    }
                } else if (line.matches("PASSED") | line.matches("FAILED")) {
                    // these will bre shown in status line anyway
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
        Collections.sort(lines, Comparator.comparing((a) -> a.getFile()));
        this.isRunning = false;
        return new MypyResult(process.exitValue(), errcount, notecount, lines);
    }

    public boolean isRunning() {
        return isRunning;
    }
}
