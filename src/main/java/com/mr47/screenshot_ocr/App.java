package com.mr47.screenshot_ocr;

import com.mr47.screenshot_ocr.config.AppConfig;
import com.mr47.screenshot_ocr.controller.Context;
import com.mr47.screenshot_ocr.ui.UI;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    public static void main(String[] args) {
        AppConfig appConfig = AppConfig.loadFromFile(args, "config.toml");
        UI.launch(appConfig.uiConfig());
        Context.prepare(appConfig.contextConfig());
    }
}
