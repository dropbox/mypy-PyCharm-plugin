package com.dropbox.plugins.mypy_plugin;

import com.dropbox.plugins.mypy_plugin.model.MypyConfig;
import com.intellij.openapi.project.Project;

public class MypyConfigLoader {
    final static String DEFAULT_MYPY_COMMAND = "dmypy start -- --follow-imports=error ; dmypy check .";
    final static String DEFAULT_MYPY_PATH_SUFFIX = "";

    public static MypyConfig findMypyConfig(Project project) {
        MypyConfigService configService = MypyConfigService.getInstance(project);
        if (configService != null && configService.getState() != null) {
            return configService.getState();
        }
        return new MypyConfig(DEFAULT_MYPY_COMMAND, DEFAULT_MYPY_PATH_SUFFIX);
    }
}
