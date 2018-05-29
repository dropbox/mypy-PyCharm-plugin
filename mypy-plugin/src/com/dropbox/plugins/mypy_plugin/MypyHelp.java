package com.dropbox.plugins.mypy_plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MypyHelp extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextPane textPane;

    public MypyHelp() {
        setContentPane(contentPane);
        setModal(false);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    public static void main(Project project) {
        MypyHelp dialog = new MypyHelp();
        dialog.pack();
        dialog.setSize(600, 400);
        JFrame frame = WindowManager.getInstance().getFrame(project);
        dialog.setLocationRelativeTo(frame);
        dialog.textPane.setCaretPosition(0);
        dialog.textPane.setForeground(new JBColor(MypyTerminal.BLACK, MypyTerminal.GRAY));
        dialog.textPane.setBackground(new JBColor(new Color(MypyTerminal.WHITE),
                dialog.contentPane.getBackground()));
        dialog.setVisible(true);
    }
}
