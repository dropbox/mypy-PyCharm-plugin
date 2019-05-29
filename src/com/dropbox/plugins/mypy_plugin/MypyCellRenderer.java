package com.dropbox.plugins.mypy_plugin;

import com.dropbox.plugins.mypy_plugin.model.MypyError;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.*;
import java.awt.*;

class MypyCellRenderer extends ColoredListCellRenderer {

    public MypyCellRenderer() {
        setOpaque(true);
    }

    @Override
    protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        int LINE_WIDTH = 5;
        int GAP = 10;
        String TAB = "     ";
        MypyError error = (MypyError) value;
        setFont(list.getFont());
        setToolTipText(null);
        boolean iserror = error.getLevel() == MypyError.ERROR;
        boolean isnote = error.getLevel() == MypyError.NOTE;
        boolean collapsed = error.isCollapsed();
        setPreferredSize(new Dimension(-1, 5));
        if (error.getLevel() == MypyError.HEADER) {
            if (collapsed) {
                setIcon(UIManager.getIcon("Tree.collapsedIcon"));
                setIconTextGap(GAP);
            } else {
                setIcon(UIManager.getIcon("Tree.expandedIcon"));
                setIconTextGap(GAP);
            }
        } else {
            setIcon(null);
        }
        if (error.getLevel() == MypyError.HEADER) {
            String file = error.getFile();
            String suffix = error.getErrcount() != 1 ? "s" : "";
            String cnt = String.format("(%d error%s)", error.getErrcount(), suffix);
            append(file + " ");
            append(cnt, new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN,
                    new Color(255, 100, 100)));
        } else if (iserror | isnote) {
            String line;
            if (error.getLine() > 0) {
                line = String.format("%d", error.getLine());
            } else {
                line = "";
            }
            append(TAB + String.format("%1$-" + LINE_WIDTH + "s", isnote ? "" : line),
                    new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN,
                            new JBColor(new Color(MypyTerminal.GRAY), new Color(MypyTerminal.DARK_GRAY))));
            if (isnote) {
                String[] chunks = error.getMessage().split("\"");
                boolean italic = false;
                for (int i = 0; i < chunks.length; i++) {
                    if (i == chunks.length - 1) {  // TODO: replace with real HTTP regex treatment.
                        if (chunks[i].matches(".+ http://.+")) {
                            String[] subchunks = chunks[i].split("http://");
                            append(subchunks[0], new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN,
                                    new JBColor(new Color(MypyTerminal.GRAY), new Color(MypyTerminal.DARK_GRAY))));
                            append(subchunks[1], SimpleTextAttributes.LINK_ATTRIBUTES);
                            setToolTipText("Alt + click to follow link");
                            break;
                        }
                    }
                    if (italic) {
                        append(chunks[i], new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC,
                                new JBColor(new Color(MypyTerminal.GRAY), new Color(MypyTerminal.DARK_GRAY))));
                    } else {
                        append(chunks[i], new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN,
                                new JBColor(new Color(MypyTerminal.GRAY), new Color(MypyTerminal.DARK_GRAY))));
                    }
                    italic = !italic;
                }
            } else {
                String[] chunks = error.getMessage().split("\"");
                boolean italic = false;
                for (int i = 0; i < chunks.length; i++) {
                    if (italic) {
                        append(chunks[i], SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES);
                    } else {
                        append(chunks[i]);
                    }
                    italic = !italic;
                }
            }
        } else {
            // something ill-formatted
            append(error.getRaw());
        }
    }
}
