package com.mr47.screenshot_ocr;

import com.mr47.screenshot_ocr.config.AppConfig;
import com.mr47.screenshot_ocr.ui.UI;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    static {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    }
    public static void main(String[] args) {
        UI.launch(AppConfig.loadFromFile(args, "config.toml"));
    }
}
