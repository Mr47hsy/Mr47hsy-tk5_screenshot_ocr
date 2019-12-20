package com.mr47.screenshot_ocr.ui;

import com.mr47.screenshot_ocr.config.UIConfig;
import com.mr47.screenshot_ocr.ui.kotlin.MainWindow;
import javafx.application.Application;

public abstract class UI extends Application {
    private static UIConfig UI_CONFIG = null;

    public static void launch(UIConfig uiConfig) {
        UI_CONFIG = uiConfig;
        Application.launch(MainWindow.class, UI_CONFIG.args());
    }

    public static UIConfig uiConfig() {
        if(UI_CONFIG == null)
            throw new NullPointerException("UI Not Launched!Call UI.launch() Method To Launch UI");
        return UI_CONFIG;
    }
}
