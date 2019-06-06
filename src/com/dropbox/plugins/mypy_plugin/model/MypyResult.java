package com.dropbox.plugins.mypy_plugin.model;

import java.util.ArrayList;

final public class MypyResult {
    private final int retCode;
    private final int errCount;
    private final int noteCount;
    private final ArrayList<MypyError> errors;

    public MypyResult(int code, int errCount, int noteCount, ArrayList<MypyError> errors) {
        this.retCode = code;
        this.errors = errors;
        this.errCount = errCount;
        this.noteCount = noteCount;
    }

    public int getRetCode() {
        return this.retCode;
    }

    public ArrayList<MypyError> getErrors() {
        return errors;
    }

    public int getErrCount() {
        return errCount;
    }

    public int getNoteCount() {
        return noteCount;
    }
}
