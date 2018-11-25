package com.dropbox.plugins.mypy_plugin.model;

import com.dropbox.plugins.mypy_plugin.MypyTerminal;
import com.intellij.openapi.editor.RangeMarker;

public class MypyError {
    public final static int DEBUG = 0;
    public final static int NOTE = 1;
    public final static int ERROR = 2;
    public final static int HEADER = -1; // not a real error, just a separator

    private int level;
    private String file;
    private int line;
    private int column;
    public RangeMarker marker;
    private String message;
    private String raw;
    // used only by headers
    private int errcount;
    private boolean collapsed;

    public MypyError(String raw, int level) {
        this.raw = raw;
        this.level = level;
        this.marker = null;
        assert((level == DEBUG) | (level == NOTE) | (level == ERROR));
        if (level == DEBUG) {
            return;
        }
        String loc;
        String[] pair;
        System.out.println(level);
        System.out.println(raw);
        if (level == NOTE) {
            pair = raw.split(MypyTerminal.NOTE_MARK);
        } else {
            pair = raw.split(MypyTerminal.ERROR_MARK);
        }
        loc = pair[0];
        if (pair.length == 1) {
            message = "";
        } else {
            message = pair[1];
        }
        String[] parts = loc.split(":");
        file = parts[0];
        if (parts.length == 1) {
            line = 0;
            column = 0;
        } else {
            try {
                line = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                line = 0;
            }
            if (parts.length == 2) {
                column = 0;
            } else {
                try {
                    column = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    column = 0;
                }
            }
        }
    }

    public MypyError(String file, int level, int errcount) {
        this.level = level;
        this.errcount = errcount;
        this.file = file;
        collapsed = false;
        assert (level == HEADER);
    }

    public String toString() {
        assert (level == DEBUG);
        // all other levels should be processed
        // by our custom renderer
        return raw;
    }

    public void toggle() {
        assert (level == HEADER);
        collapsed = !collapsed;
    }

    public int getLevel() {
        return level;
    }

    public String getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getMessage() {
        return message;
    }

    public String getRaw() {
        return raw;
    }

    public int getErrcount() {
        return errcount;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public boolean isError() {
        return level == NOTE || level == ERROR;
    }
}
