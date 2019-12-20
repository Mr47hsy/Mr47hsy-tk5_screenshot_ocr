package com.mr47.screenshot_ocr.controller;

import com.mr47.screenshot_ocr.config.ContextConfig;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.concurrent.TimeUnit;

public abstract class Context {
    private static ContextConfig CONTEXT_CONFIG = null;
    private static Vertx vertx = null;

    private WebClient webClient;

    public Context() {
        int connectTimeOut = CONTEXT_CONFIG.callTimeOut();
        if(CONTEXT_CONFIG.callTimeOutUnit() != TimeUnit.MILLISECONDS)
            connectTimeOut = (int) CONTEXT_CONFIG.callTimeOutUnit()
                    .toMillis(connectTimeOut);

        webClient = WebClient.create(vertx, new WebClientOptions()
                .setConnectTimeout(connectTimeOut));

        if(CONTEXT_CONFIG.baiduAI().autoRefreshToken()){
            long refreshTokenInterval = CONTEXT_CONFIG.baiduAI().refreshTokenInterval();
            if(CONTEXT_CONFIG.baiduAI().refreshTokenIntervalUnit() != TimeUnit.MILLISECONDS)
                refreshTokenInterval = CONTEXT_CONFIG.baiduAI().refreshTokenIntervalUnit()
                        .toMillis(refreshTokenInterval);

            vertx.setPeriodic(refreshTokenInterval, t -> refreshToken());
        }
    }

    public static void prepare(ContextConfig contextConfig) {
        CONTEXT_CONFIG = contextConfig;

        vertx = Vertx.vertx(
                new VertxOptions()
                        .setEventLoopPoolSize(CONTEXT_CONFIG.eventLoop().size())
                        .setBlockedThreadCheckInterval(CONTEXT_CONFIG.eventLoop().blockedCheckInterval())
                        .setBlockedThreadCheckIntervalUnit(CONTEXT_CONFIG.eventLoop().blockedCheckIntervalUnit())
        );
    }

    public static ContextConfig contextConfig() {
        return CONTEXT_CONFIG;
    }

    public WebClient webClient() {
        return webClient;
    }

    protected abstract void refreshToken();
}
