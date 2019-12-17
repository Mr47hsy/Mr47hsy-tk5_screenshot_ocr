package com.mr47.screenshot_ocr.controller.kotlin

import com.mr47.screenshot_ocr.controller.Context
import com.mr47.screenshot_ocr.proxy.Service
import com.mr47.screenshot_ocr.proxy.kotlin.BaiduAI

class MainWindowController: Context() {

    override fun refreshToken() {
        Service.proxy(this, BaiduAI::class.java).getAccessToken()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}