package com.mr47.screenshot_ocr.controller.kotlin

import animatefx.animation.FadeIn
import animatefx.animation.RotateIn
import com.jfoenix.animation.alert.JFXAlertAnimation
import com.jfoenix.controls.*
import com.mr47.screenshot_ocr.controller.Context
import com.mr47.screenshot_ocr.controller.Controller
import com.mr47.screenshot_ocr.proxy.BaiduAIException
import com.mr47.screenshot_ocr.proxy.Service
import com.mr47.screenshot_ocr.proxy.kotlin.BaiduAI
import com.mr47.screenshot_ocr.ui.UI
import com.mr47.screenshot_ocr.util.StrUtil
import com.mr47.screenshot_ocr.util.TimeUtil
import io.vertx.core.buffer.Buffer
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import javafx.application.Platform
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Modality
import javafx.stage.Stage
import kotlinx.coroutines.*
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.util.NoSuchElementException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class MainWindowController: Controller {
    private data class Image(val name: String, val data: Buffer)

    private lateinit var context: Context

    lateinit var root: VBox
    lateinit var openButton: JFXRippler
    lateinit var openImageView: ImageView
    lateinit var openLabel: Label
    lateinit var openedMessageLabel: Label
    lateinit var startHBox: HBox
    lateinit var startButton: JFXRippler
    lateinit var startImageView: ImageView
    lateinit var startLabel: Label

    private val progressBar = JFXProgressBar()
    private val spinner = JFXSpinner()

    private var openButtonEnabled = true
    private var startButtonEnabled = true

    /*      Data      */
    private var token: String = ""
    private val counter = AtomicInteger(0)
    private val imageList = arrayListOf<String>()
    private val queue = ConcurrentLinkedQueue<Image>()
    private val loadIndex = AtomicInteger(0)
    private val running = AtomicBoolean(false)

    @ExperimentalCoroutinesApi
    override fun init(context: Context?) {
        this.context = context!!
        openImageView.image = Image(
                "file:${UI.uiConfig().assent().dir()}/open.png",
                35.0, 35.0,
                true, false
        )
        startImageView.image = Image(
                "file:${UI.uiConfig().assent().dir()}/startOCR.png",
                50.0, 50.0,
                true, false
        )

        openButton.setOnMouseClicked{
            if(openButtonEnabled) openDir()
        }
        startButton.setOnMouseClicked{
            if(startButtonEnabled) startOCR()
        }
        startButtonEnabled = false

        progressBar.progress = 0.0
        progressBar.style = "-fx-padding: 25"
        progressBar.prefHeight = 60.0

        root.children.add(spinner)
        openButton.setEnabled(false)
        GlobalScope.launch (Context.vertx().dispatcher()) {
            val stage = root.scene.window as Stage
            try {
                loadCache()
                Platform.runLater{ openButton.setEnabled(true) }
            } catch (e: BaiduAIException) {
                Platform.runLater { showAlert(
                        stage,
                        "请求百度AI失败",
                        "方法：getAccessToken 错误：${e.useCn().message}"
                ) }
            } catch (e: Exception) {
                Platform.runLater { showAlert(
                        stage,
                        "程序内部错误",
                        "错误信息：${e.message}"
                ) }
            } finally {
                Platform.runLater { root.children.remove(spinner) }
            }
        }
    }

    private fun openDir() {
        val stage = root.scene.window as Stage
        val chooser = DirectoryChooser()
        val file: File? = chooser.showDialog(stage)
        val imageFiles: Array<File>? = file?.listFiles{_, name ->
            val fileExtension = StrUtil.getFileExtension(name)
            return@listFiles (fileExtension == ".jpg") || (fileExtension == ".JPG")
                    || (fileExtension == ".jpge") || (fileExtension == ".JPGE")
                    || (fileExtension == ".png") || (fileExtension == ".PNG")
        }
        when {
            (imageFiles == null) || (imageFiles.isEmpty()) -> {
                showAlert(stage, "打开文件夹错误", "没有发现格式为jpg,jpge或者png的图片")
                reset()
            }
            else -> {
                for(f in imageFiles) {
                    imageList.add(f.path)
                }
                openLabel.text = "再次打开..."
                openedMessageLabel.text = "检索文件夹成功.共发现${imageFiles.size}张可识别图片"
                FadeIn(openedMessageLabel).play()
                startButtonEnabled = true
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun startOCR() {
        startImageView.image = Image(
                "file:${UI.uiConfig().assent().dir()}/startingOCR.png",
                50.0, 50.0,
                true, false
        )
        RotateIn(startImageView).play()
        startLabel.text = "正在识别..."
        startButtonEnabled = false
        startHBox.children.add(progressBar)
        openButtonEnabled = false
        root.children.add(spinner)

        val future = GlobalScope.async (Context.vertx().dispatcher()) {
            val outputDir = initQueue()
            openCallFromQueue(outputDir)
            return@async outputDir
        }
        future.invokeOnCompletion {
            val stage = root.scene.window as Stage
            val error = future.getCompletionExceptionOrNull()
            Platform.runLater {
                root.children.remove(spinner)
                if(error != null)
                    showAlert(stage, "程序内部错误", "错误信息：${error.message}")
                else showAlert(
                        stage,
                        "识别结束",
                        "一共识别${counter.get()}张图片.前往${future.getCompleted()}目录查看识别结果."
                )
                reset()
                counter.set(0)
                imageList.clear()
                loadIndex.set(0)
            }
        }
    }

    private suspend fun initQueue(): String = coroutineScope {
        val outputDir = "${Context.contextConfig().outputDir()}/" +
                "${TimeUtil.getCurrentDate(false)}_${StrUtil.generateShortUuid()}"
        awaitResult<Void> { Context.vertx().fileSystem().mkdir(outputDir, it) }

        val queueSize = Context.contextConfig().imageQueueSize()
        val futures: ArrayList<Deferred<Image>>
        if(imageList.size >= queueSize){
            futures = ArrayList(queueSize)
            for((i, image) in imageList.withIndex()) {
                if(i >= queueSize)
                    break
                futures.add(async {
                    val buffer = awaitResult<Buffer> { h ->
                        Context.vertx().fileSystem().readFile(image, h)
                    }
                    Image(File(image).name, buffer)
                })
            }
            loadIndex.set(queueSize - 1)
        } else {
            futures = ArrayList(imageList.size)
            for(image in imageList) {
                futures.add(async {
                    val buffer = awaitResult<Buffer> { h ->
                        Context.vertx().fileSystem().readFile(image, h)
                    }
                    Image(File(image).name, buffer)
                })
            }
            loadIndex.set(imageList.lastIndex)
        }

        for(future in futures) {
            queue.add(future.await())
        }

        return@coroutineScope outputDir
    }

    private suspend fun openCallFromQueue(outputDir: String): Unit = coroutineScope {
        val concurrentCallNumber = Context.contextConfig()
                .concurrentCallNumber()
        running.set(true)
        val futures = ArrayList<Deferred<Unit>>(concurrentCallNumber)
        try {
            for(i in 0 until concurrentCallNumber) {
                val image = queue.remove()
                futures.add(async { call(outputDir, image) })
            }
        } catch (e: NoSuchElementException) {
            running.set(false)
        } finally {
            for (future in futures) future.await()
            if(imageList.size > counter.get()) openCallFromQueue(outputDir)
        }
    }

    private suspend fun call(outputDir: String, image: Image) {
        val fileSystem = Context.vertx().fileSystem()
        try {
            val words = Service.proxy(context, BaiduAI::class.java)
                    .generalBasic(token, image.data)
            val builder = StringBuilder()
            for(word in words) {
                builder.append(word)
                        .append("\n")
            }
            val buffer = Buffer.buffer(builder.toString())
            awaitResult<Void> {
                fileSystem.writeFile("$outputDir/${image.name}.txt", buffer, it)
            }
        } catch (e: BaiduAIException) {
            val buffer = Buffer.buffer("BaiduAI调用出现错误.\n" +
                    "error code: ${e.errorCode()} error message: ${e.useCn().message}")
            awaitResult<Void> { fileSystem.writeFile("$outputDir/${image.name}.txt", buffer, it) }
        } finally {
            counter.getAndIncrement()
            Platform.runLater {
                progressBar.progress = counter.toDouble() / imageList.size.toDouble()
            }
            if(imageList.size > (loadIndex.get() + 1)) loadOne(outputDir, fileSystem)
        }
    }

    private suspend fun loadOne(outputDir: String, fileSystem: FileSystem) {
        val index = loadIndex.incrementAndGet()
        val buffer = awaitResult<Buffer> { fileSystem.readFile(imageList[index], it) }
        try {
            queue.add(Image(File(imageList[index]).name, buffer))
        } catch (e: IllegalStateException) {
        }finally {
            if (!running.get()) openCallFromQueue(outputDir)
        }
    }

    private fun reset() {
        openLabel.text = "打开文件夹..."
        openButtonEnabled = true
        openedMessageLabel.text = ""

        startHBox.children.remove(progressBar)
        startImageView.image = Image(
                "file:${UI.uiConfig().assent().dir()}/startOCR.png",
                50.0, 50.0,
                true, false
        )
        startLabel.text = "开始识别"
        startButtonEnabled = false

        progressBar.progress = 0.0
    }

    private suspend fun loadCache() {
        val contextConfig = Context.contextConfig()
        val refreshTokenIntervalMills = contextConfig.baiduAI()
                .refreshTokenIntervalUnit()
                .toMillis(contextConfig.baiduAI()
                        .refreshTokenInterval())

        token = try {
            val cache = context.recentlyCache().await()
            val difference = System.currentTimeMillis() - cache.saveTimestamp
            if(difference < refreshTokenIntervalMills) {
                cache.data.getString("token")
            }else {
                throw Exception("Cache Token Can't Useful")
            }
        } catch (e: Exception) {
            val newToken = Service.proxy(context, BaiduAI::class.java).getAccessToken()
            Context.Cache(
                    System.currentTimeMillis(),
                    JsonObject().put("token", newToken)
            ).save().await()
            newToken
        }

        if(contextConfig.baiduAI().autoRefreshToken()){
            context.setPeriodic("refresh_token", refreshTokenIntervalMills) {
                GlobalScope.launch (Context.vertx().dispatcher()) {
                    try {
                        token = Service.proxy(context, BaiduAI::class.java).getAccessToken()
                        Context.Cache(
                                System.currentTimeMillis(),
                                JsonObject().put("token", token)
                        ).save().await()
                    } catch (e: BaiduAIException) {
                        Platform.runLater {
                            val stage = root.scene.window as Stage
                            showAlert(stage, "请求百度AI失败", "方法：getAccessToken 错误：${e.useCn().message}")
                        }
                        throw e
                    }
                }
            }
        }
    }

    private fun showAlert(stage: Stage, head: String, body: String) {
        val layout = JFXDialogLayout()
        layout.setHeading(Label(head))
        layout.setBody(Label(body))
        val alert = JFXAlert<Void>(stage)
        val closeButton = JFXButton("好的")
        closeButton.buttonType = JFXButton.ButtonType.RAISED
        closeButton.style = "-fx-text-fill: #03A9F4; -fx-font-weight: BOLD"
        closeButton.setOnAction { alert.hideWithAnimation() }
        layout.setActions(closeButton)
        alert.isOverlayClose = false
        alert.animation = JFXAlertAnimation.CENTER_ANIMATION
        alert.setContent(layout)
        alert.initModality(Modality.APPLICATION_MODAL)
        alert.setSize(300.0, 200.0)
        alert.show()
    }
}