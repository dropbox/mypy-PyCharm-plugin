package com.dropbox.plugins.mypy_plugin;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name="MypyConfigService",
        storages = {
                @Storage("MypyConfig.xml")}
)
public final class MypyConfigService implements PersistentStateComponent<MypyConfigService> {
    private String executableName;
    private String pathSuffix;

    String getExecutableName() {
        return executableName;
    }

    void setExecutableName(String executableName) {
        this.executableName = executableName;
    }

    String getPathSuffix() {
        return pathSuffix;
    }

    void setPathSuffix(String pathSuffix) {
        this.pathSuffix = pathSuffix;
    }

    @NotNull
    @Override
    public MypyConfigService getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull MypyConfigService config) {
        XmlSerializerUtil.copyBean(config, this);
    }

    @Nullable
    public static MypyConfigService getInstance(Project project) {
        return ServiceManager.getService(project, MypyConfigService.class);
    }
}