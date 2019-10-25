package com.dropbox.plugins.mypy_plugin;

import com.dropbox.plugins.mypy_plugin.model.MypyConfig;
import com.intellij.openapi.project.Project;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;

public class MypyConfigLoader {
    final static String DEFAULT_MYPY_COMMAND = "dmypy run -- --follow-imports=error .";
    final static String DEFAULT_MYPY_PATH_SUFFIX = "";

    public static MypyConfig findMypyConfig(Project project) {
        MypyConfigService configService = MypyConfigService.getInstance(project);
        if (configService != null && configService.getState() != null) {
            return configService.getState();
        }
        MypyConfig iniConfig = loadConfigFromIni(project);
        if (iniConfig != null) {
            return iniConfig;
        }
        return new MypyConfig(DEFAULT_MYPY_COMMAND, DEFAULT_MYPY_PATH_SUFFIX);
    }

    public static MypyConfig loadConfigFromIni(Project project) {
        String directory = project.getBaseDir().getPath();
        File ini_file = new File(directory, "mypy.ini");
        Ini ini = null;
        try {
            ini = new Ini(ini_file);
        } catch (IOException e) {
            return null;
        }
        String command = ini.get("mypy", "x_pycharm_command");
        String path_suffix = ini.get("mypy", "x_pycharm_PATH");
        return new MypyConfig(
                command != null ? command : DEFAULT_MYPY_COMMAND,
                path_suffix != null ? path_suffix : DEFAULT_MYPY_PATH_SUFFIX);

    }
}
