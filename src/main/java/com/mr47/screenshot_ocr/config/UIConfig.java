package com.mr47.screenshot_ocr.config;

import com.moandjiezana.toml.Toml;

public class UIConfig implements TomlConfig {
    private String[] appStartArgs;

    private Assent assent;

    public UIConfig(String[] appStartArgs) {
        this.appStartArgs = appStartArgs;

        assent = new Assent();
    }

    @SuppressWarnings("unchecked")
    @Override
    public UIConfig mergeFromToml(Toml toml) {
        if(toml.containsTable("assent"))
            assent.mergeFromToml(toml.getTable("assent"));

        return this;
    }

    public String[] args() {
        return appStartArgs;
    }

    public Assent assent() {
        return assent;
    }

    public static class Assent implements TomlConfig {
        private String dir;
        private String fxmlFileName;

        private Assent() {
            dir = "assent";
            fxmlFileName = "assent/main_window.fxml";
        }

        @SuppressWarnings("unchecked")
        @Override
        public Assent mergeFromToml(Toml toml) {
            if(toml.contains("dir"))
                dir = toml.getString("dir");

            if(toml.contains("fxml"))
                fxmlFileName = toml.getString("fxml");

            return this;
        }

        public String dir() {
            return dir;
        }

        public String fxmlFileName() {
            return fxmlFileName;
        }
    }
}
