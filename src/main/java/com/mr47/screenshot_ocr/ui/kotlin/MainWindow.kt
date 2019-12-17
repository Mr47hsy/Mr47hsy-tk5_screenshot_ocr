package com.mr47.screenshot_ocr.ui.kotlin

import com.mr47.screenshot_ocr.controller.kotlin.MainWindowController
import com.mr47.screenshot_ocr.ui.UI
import javafx.fxml.FXMLLoader
import javafx.fxml.JavaFXBuilderFactory
import javafx.scene.Parent
import javafx.stage.Stage
import java.io.FileInputStream

class MainWindow: UI() {

    override fun start(primaryStage: Stage?) {
        primaryStage!!
        val fxmlLoader = FXMLLoader()
        fxmlLoader.builderFactory = JavaFXBuilderFactory()

        val inputStream = FileInputStream("${uiConfig().assent().dir()}/${uiConfig().assent().fxmlFileName()}")
        inputStream.use {
            val root = fxmlLoader.load<Parent>(it)
            prepareShow(primaryStage, root, fxmlLoader.getController())
        }
    }

    private fun prepareShow(stage: Stage, root: Parent, controller: MainWindowController) {

    }
}