package com.dropbox.plugins.mypy_plugin;

import com.dropbox.plugins.mypy_plugin.model.MypyConfig;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import icons.MypyIcons;

import javax.swing.*;

public final class MypyConfigDialog extends DialogWrapper {
    private JPanel contentPane;
    private JLabel logo;
    private JTextField command;
    private JTextField path;
    private final MypyConfigService configService;

    MypyConfigDialog(Project project) {
        super(project);
        setModal(true);
        init();
        setTitle("Mypy Plugin Configuration");
        configService = MypyConfigService.getInstance(project);
        MypyConfig config = MypyConfigLoader.findMypyConfig(project);
        this.command.setText(config.getExecutableName());
        this.path.setText(config.getPathSuffix());
        logo.setIcon(MypyIcons.MYPY_BIG);
        command.setCaretPosition(0);
        path.setCaretPosition(0);
    }

    @Override
    protected void doOKAction() {
        configService.loadState(new MypyConfig(command.getText(), path.getText()));
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
