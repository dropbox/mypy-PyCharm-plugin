package com.dropbox.plugins.mypy_plugin;

import com.dropbox.plugins.mypy_plugin.model.MypyError;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

final class MypyCellRenderer extends ColoredListCellRenderer<MypyError> {
    MypyCellRenderer() {
        setOpaque(true);
    }

    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends MypyError> list, MypyError value, int index, boolean selected, boolean hasFocus) {
        int LINE_WIDTH = 5;
        int GAP = 10;
        String TAB = "     ";
        setFont(list.getFont());
        setToolTipText(null);
        boolean isError = value.getLevel() == MypyError.ERROR;
        boolean isNote = value.getLevel() == MypyError.NOTE;
        boolean collapsed = value.isCollapsed();
        setPreferredSize(new Dimension(-1, 5));
        if (value.getLevel() == MypyError.HEADER) {
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
        if (value.getLevel() == MypyError.HEADER) {
            String file = value.getFile();
            String suffix = value.getErrCount() != 1 ? "s" : "";
            String cnt = String.format("(%d error%s)", value.getErrCount(), suffix);
            append(file + " ");
            append(cnt, new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN,
                    new JBColor(new Color(255, 100, 100), new Color(255, 100, 100))));
        } else if (isError | isNote) {
            String line;
            if (value.getLine() > 0) {
                line = String.format("%d", value.getLine());
            } else {
                line = "";
            }
            append(TAB + String.format("%1$-" + LINE_WIDTH + "s", isNote ? "" : line),
                    new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN,
                            new JBColor(new Color(MypyTerminal.GRAY), new Color(MypyTerminal.DARK_GRAY))));
            if (isNote) {
                String[] chunks = value.getMessage().split("\"");
                boolean italic = false;
                for (int i = 0; i < chunks.length; i++) {
                    if (i == chunks.length - 1) {  // TODO: replace with real HTTP regex treatment.
                        if (chunks[i].matches(".+ http://.+")) {
                            String[] subChunks = chunks[i].split("http://");
                            append(subChunks[0], new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN,
                                    new JBColor(new Color(MypyTerminal.GRAY), new Color(MypyTerminal.DARK_GRAY))));
                            append(subChunks[1], SimpleTextAttributes.LINK_ATTRIBUTES);
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
                String[] chunks = value.getMessage().split("\"");
                boolean italic = false;
                for (String chunk : chunks) {
                    if (italic) {
                        append(chunk, SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES);
                    } else {
                        append(chunk);
                    }
                    italic = !italic;
                }
            }
        } else {
            // something ill-formatted
            append(value.getRaw());
        }
    }
}
