package com.mr47.screenshot_ocr.config;

import com.moandjiezana.toml.Toml;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class AppConfig implements TomlConfig{
    private TomlConfig uiConfig;
    private TomlConfig contextConfig;

    public AppConfig(String[] appStartArgs) {
        uiConfig = new UIConfig(appStartArgs);
        contextConfig = new ContextConfig();
    }

    public static AppConfig loadFromFile(String[] appStartArgs, String configFilePath) {
        return new AppConfig(appStartArgs)
                .mergeFromToml(new Toml().read(new File(configFilePath)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public AppConfig mergeFromToml(Toml toml) {
        if(toml.containsTable("ui"))
            uiConfig.mergeFromToml(toml.getTable("ui"));

        if(toml.containsTable("context"))
            contextConfig.mergeFromToml(toml.getTable("context"));
        return this;
    }

    public UIConfig uiConfig() {
        return (UIConfig) uiConfig;
    }

    public ContextConfig contextConfig() {
        return (ContextConfig) contextConfig;
    }
}
