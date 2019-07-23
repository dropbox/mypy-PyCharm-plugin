package com.dropbox.plugins.mypy_plugin.model;

public class MypyConfig {
    public String executableName;
    public String pathSuffix;

    public MypyConfig() {
        // This is so the serialization system can handle this
    }

    public MypyConfig(String executableName, String pathSuffix) {
        this.executableName = executableName;
        this.pathSuffix = pathSuffix;
    }

    public String getExecutableName() {
        return executableName;
    }

    public String getPathSuffix() {
        return pathSuffix;
    }
}
