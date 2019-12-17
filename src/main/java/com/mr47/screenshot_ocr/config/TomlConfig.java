package com.mr47.screenshot_ocr.config;

import com.moandjiezana.toml.Toml;

public interface TomlConfig {
    public <R extends TomlConfig> R mergeFromToml(Toml toml);
}
