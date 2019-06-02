package com.dropbox.plugins.mypy_plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import icons.MypyIcons;

import javax.swing.*;

public final class MypyConfig extends DialogWrapper {
    private JPanel contentPane;
    private JLabel logo;
    private JTextField command;
    private JTextField path;
    private final MypyConfigService config;

    MypyConfig(Project project) {
        super(project);
        setModal(true);
        init();
        setTitle("Mypy Plugin Configuration");
        config = MypyConfigService.getInstance(project);
        String storedCmd = config != null ? config.getExecutableName() : null;
        if (storedCmd != null) {
            this.command.setText(storedCmd);
        } else {
            this.command.setText(MypyToolWindowFactory.DEFAULT_MYPY_COMMAND);
        }
        String storedPath = config != null ? config.getPathSuffix() : null;
        if (storedPath != null) {
            this.path.setText(storedPath);
        } else {
            this.path.setText(MypyToolWindowFactory.DEFAULT_MYPY_PATH_SUFFIX);
        }
        logo.setIcon(MypyIcons.MYPY_BIG);
        command.setCaretPosition(0);
        path.setCaretPosition(0);
    }

    @Override
    protected void doOKAction() {
        config.setExecutableName(command.getText());
        config.setPathSuffix(path.getText());
        super.doOKAction();
    }

    public JComponent createCenterPanel() {
        return this.contentPane;
    }

    public ValidationInfo doValidate() {
        if(this.command.getText().equals("")) {
            return new ValidationInfo("Command cannot be empty", this.command);
        }
        return null;
    }

}
