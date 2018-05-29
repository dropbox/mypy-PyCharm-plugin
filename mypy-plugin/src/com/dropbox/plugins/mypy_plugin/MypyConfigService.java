package com.dropbox.plugins.mypy_plugin;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(
        name="MypyConfigService",
        storages = {
                @Storage("MypyConfig.xml")}
)
public class MypyConfigService implements PersistentStateComponent<MypyConfigService> {

    String executableName;
    String pathSuffix;

    public String getExecutableName() {
        return executableName;
    }

    public void setExecutableName(String executableName) {
        this.executableName = executableName;
    }

    public String getPathSuffix() {
        return pathSuffix;
    }

    public void setPathSuffix(String pathSuffix) {
        this.pathSuffix = pathSuffix;
    }

    @Nullable
    @Override
    public MypyConfigService getState() {
        return this;
    }

    @Override
    public void loadState(MypyConfigService config) {
        XmlSerializerUtil.copyBean(config, this);
    }

    @Nullable
    public static MypyConfigService getInstance(Project project) {
        return ServiceManager.getService(project, MypyConfigService.class);
    }
}