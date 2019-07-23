package com.dropbox.plugins.mypy_plugin;

import com.dropbox.plugins.mypy_plugin.model.MypyConfig;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "MypyConfigService",
        storages = {
                @Storage("MypyConfig.xml")}
)
public final class MypyConfigService implements PersistentStateComponent<MypyConfig> {
    private MypyConfig config;

    @Nullable
    @Override
    public MypyConfig getState() {
        return config;
    }

    @Override
    public void loadState(@NotNull MypyConfig config) {
        this.config = config;
    }

    @Nullable
    public static MypyConfigService getInstance(Project project) {
        return ServiceManager.getService(project, MypyConfigService.class);
    }
}