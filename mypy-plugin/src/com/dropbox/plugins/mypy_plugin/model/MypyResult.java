package com.dropbox.plugins.mypy_plugin.model;

import java.util.ArrayList;

public class MypyResult {
    private int retCode;
    private int errcount;
    private int notecount;
    private ArrayList<MypyError> errors;

    public MypyResult(int code, int errcount, int notecount, ArrayList<MypyError> errors) {
        this.retCode = code;
        this.errors = errors;
        this.errcount = errcount;
        this.notecount = notecount;
    }

    public int getRetCode() {
        return this.retCode;
    }

    public ArrayList<MypyError> getErrors() {
        return errors;
    }

    public int getErrcount() {
        return errcount;
    }

    public int getNotecount() {
        return notecount;
    }
}
