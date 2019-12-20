package com.mr47.screenshot_ocr.ui;

import com.mr47.screenshot_ocr.config.AppConfig;
import com.mr47.screenshot_ocr.config.UIConfig;
import com.mr47.screenshot_ocr.controller.Context;
import com.mr47.screenshot_ocr.ui.kotlin.MainWindow;
import javafx.application.Application;

public abstract class UI extends Application {
    private static UIConfig UI_CONFIG = null;

    public static void launch(AppConfig appConfig) {
        UI_CONFIG = appConfig.uiConfig();
        Context.prepare(appConfig.contextConfig());
        Application.launch(MainWindow.class, UI_CONFIG.args());
    }

    public static UIConfig uiConfig() {
        if(UI_CONFIG == null)
            throw new NullPointerException("UI Not Launched!Call UI.launch() Method To Launch UI");
        return UI_CONFIG;
    }
}
