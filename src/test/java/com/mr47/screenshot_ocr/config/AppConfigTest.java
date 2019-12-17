package com.mr47.screenshot_ocr.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void loadFromFile() {
        String[] args = {"test"};
        AppConfig.loadFromFile(args, "config.toml");
    }
}