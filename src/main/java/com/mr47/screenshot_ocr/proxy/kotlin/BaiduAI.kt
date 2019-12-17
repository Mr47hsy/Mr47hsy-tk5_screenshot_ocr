package com.mr47.screenshot_ocr.proxy.kotlin

import com.mr47.screenshot_ocr.proxy.Service
import io.vertx.core.buffer.Buffer

interface BaiduAI: Service {
    public suspend fun getAccessToken(): String

    //low precision
    public suspend fun generalBasic(token: String, image: Buffer): List<String>

    //high precision
    public suspend fun accurateBasic(token: String, image: Buffer): List<String>
}