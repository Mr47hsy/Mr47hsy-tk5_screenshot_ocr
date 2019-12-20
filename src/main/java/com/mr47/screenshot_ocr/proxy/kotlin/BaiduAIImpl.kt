package com.mr47.screenshot_ocr.proxy.kotlin

import com.mr47.screenshot_ocr.controller.Context
import com.mr47.screenshot_ocr.proxy.BaiduAIException
import io.vertx.core.MultiMap
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.awaitResult
import java.util.*
import kotlin.collections.ArrayList

class BaiduAIImpl(private val webClient: WebClient): BaiduAI {

    override suspend fun getAccessToken(): String {
        val url = "https://aip.baidubce.com/oauth/2.0/token"
        val request = webClient.postAbs(url)
                .addQueryParam("grant_type", "client_credentials")
                .addQueryParam("client_id", Context.contextConfig().baiduAI().apiKey())
                .addQueryParam("client_secret", Context.contextConfig().baiduAI().secretKey())
        val jsonObject = awaitResult<HttpResponse<Buffer>> { h -> request.send(h) }
                .bodyAsJsonObject()
        return when {
            jsonObject.containsKey("access_token") -> jsonObject.getString("access_token")!!
            jsonObject.containsKey("error") -> when (jsonObject.getString("error_description")!!) {
                "unknown client id" -> throw BaiduAIException(1, "unknown client id", "API Key不正确")
                "Client authentication failed" -> throw BaiduAIException(1, "Client authentication failed", "Secret Key不正确")
                else -> throw BaiduAIException(0, "Unknown error", "抱歉,发生未知错误")
            }
            else -> throw BaiduAIException(0, "Unknown error", "抱歉,发生未知错误")
        }
    }

    override suspend fun generalBasic(token: String, image: Buffer): List<String> {
        val url = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic"
        return ocr(token, image, url)
    }

    override suspend fun accurateBasic(token: String, image: Buffer): List<String> {
        val url = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic"
        return ocr(token, image, url)
    }

    private suspend fun ocr(token: String, image: Buffer, url: String): List<String> {
        if(image.bytes.size > 2097152) throw BaiduAIException(0, "The image size is over 2M", "图片大小超过2M,识别失败")
        val base64 = Base64.getEncoder().encodeToString(image.bytes)
        val request = webClient.postAbs(url)
                .addQueryParam("access_token", token)
        val map = MultiMap.caseInsensitiveMultiMap()
        map.add("image", base64)
        val jsonObject = awaitResult<HttpResponse<Buffer>> { h -> request.sendForm(map, h) }
                .bodyAsJsonObject()
        when {
            jsonObject.containsKey("words_result") -> {
                val list = ArrayList<String>(jsonObject.getInteger("words_result_num")!!)
                val wordsResult = jsonObject.getJsonArray("words_result")!!
                for (i in wordsResult.list.indices) {
                    val words = wordsResult.getJsonObject(i)!!
                            .getString("words")!!
                    list.add(words)
                }
                return list
            }
            jsonObject.containsKey("error_code") ->
                throw getBaiduAIExceptionFromErrorCode(jsonObject.getInteger("error_code"))
            else -> throw BaiduAIException(0, "Unknown error", "抱歉,发生未知错误")
        }
    }

    private fun getBaiduAIExceptionFromErrorCode(errorCode: Int): BaiduAIException {
        return when (errorCode) {
            1 -> BaiduAIException(1, "Unknown error", "服务器内部错误")
            2 -> BaiduAIException(2, "Service temporarily unavailable", "服务暂不可用")
            3 -> BaiduAIException(3, "Unsupported openapi method", "调用的API不存在，请检查后重新尝试")
            4 -> BaiduAIException(4, "Open api request limit reached", "集群超限额")
            17 -> BaiduAIException(17, "Open api daily request limit reached", "每天请求量超限额")
            18 -> BaiduAIException(18, "Open api qps request limit reached", "QPS超限额")
            19 -> BaiduAIException(19, "Open api total request limit reached", "请求总量超限额")
            100 -> BaiduAIException(100, "Invalid parameter", "无效的access_token参数，请检查后重新尝试")
            110 -> BaiduAIException(110, "Access token invalid or no longer valid", "access_token无效")
            111 -> BaiduAIException(111, "Access token expired", "access token过期")
            else -> BaiduAIException(errorCode, "Unknown error", "抱歉,发生未知错误")
        }
    }
}