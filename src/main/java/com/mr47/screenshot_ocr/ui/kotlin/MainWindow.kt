package com.mr47.screenshot_ocr.ui.kotlin

import com.mr47.screenshot_ocr.controller.Context
import com.mr47.screenshot_ocr.controller.Controller
import com.mr47.screenshot_ocr.ui.UI
import javafx.fxml.FXMLLoader
import javafx.fxml.JavaFXBuilderFactory
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import java.io.FileInputStream

class MainWindow: UI() {
    private val context = Context.context()
    private lateinit var root: Parent
    private lateinit var mainWindowController: Controller

    override fun init() {
        val fxmlLoader = FXMLLoader()
        fxmlLoader.builderFactory = JavaFXBuilderFactory()

        val inputStream = FileInputStream("${uiConfig().assent().dir()}/${uiConfig().assent().fxmlFileName()}")
        inputStream.use {
            root = fxmlLoader.load<Parent>(it)
            mainWindowController = fxmlLoader.getController()
        }
    }

    override fun start(primaryStage: Stage?) {
        primaryStage!!.title = "图片识别工具"
        primaryStage.scene = Scene(root)
        primaryStage.isResizable = false
        mainWindowController.init(context)
        primaryStage.show()
    }

    override fun stop() {
        context.close()
    }
}