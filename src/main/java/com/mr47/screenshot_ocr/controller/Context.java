package com.mr47.screenshot_ocr.controller;

import com.mr47.screenshot_ocr.config.ContextConfig;
import com.mr47.screenshot_ocr.util.StrUtil;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Context {
    private static ContextConfig CONTEXT_CONFIG = null;
    private static Vertx vertx = null;

    private WebClient webClient;
    private Map<String, Long> timerIDs;

    private Context() {
        timerIDs = new HashMap<>();

        int connectTimeOut = CONTEXT_CONFIG.callTimeOut();
        if(CONTEXT_CONFIG.callTimeOutUnit() != TimeUnit.MILLISECONDS)
            connectTimeOut = (int) CONTEXT_CONFIG.callTimeOutUnit()
                    .toMillis(connectTimeOut);

        webClient = WebClient.create(vertx, new WebClientOptions()
                .setConnectTimeout(connectTimeOut));
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

    public static Context context() {
        if(CONTEXT_CONFIG == null)
            throw new NullPointerException("ContextConfig Is Null.Call Context.prepare() Method To Set ContextConfig");
        else if(vertx == null)
            throw new NullPointerException("Vertx Is Null.Call Context.prepare() Method To Init Vertx");
        else return new Context();
    }

    public Context cancelTimer(String timerID) {
        if(timerIDs.containsKey(timerID)){
            vertx.cancelTimer(timerIDs.get(timerID));
            timerIDs.remove(timerID);
        }

        return this;
    }

    public Context close() {
        webClient.close();

        for(Long timerIDValue: timerIDs.values()) {
            vertx.cancelTimer(timerIDValue);
        }
        timerIDs.clear();
        vertx.close();

        return this;
    }

    public Context setPeriodic(String timerID, long interval, Consumer<String> fn) {
        timerIDs.put(timerID, vertx.setPeriodic(interval, t -> fn.accept(timerID)));
        return this;
    }

    public static Vertx vertx() {
        return vertx;
    }

    public static ContextConfig contextConfig() {
        return CONTEXT_CONFIG;
    }

    public Future<Cache> recentlyCache() {
        return recentlyCacheByTimestamp(System.currentTimeMillis());
    }

    public Future<Cache> recentlyCacheByTimestamp(long timestamp) {
        return Future.<List<String>>future(promise ->
                vertx.fileSystem().readDir(CONTEXT_CONFIG.cacheDir(), promise)
        ).compose(files -> {
            try {
                return Future.succeededFuture(
                        getClosestFileFromFilesByTimestamp(timestamp, files)
                );
            } catch (Exception e) {
                return Future.failedFuture(e);
            }
        }).compose(file ->
                Future.<Buffer>future(promise ->
                        vertx.fileSystem().readFile(file, promise)
                ).compose(buffer -> {
                    String fileName = fileName(file);
                    long fileTimestamp = Long.parseUnsignedLong(fileName);
                    return Future.succeededFuture(
                            new Cache(fileTimestamp, buffer.toJsonObject())
                    );
                })
        );
    }

    public WebClient webClient() {
        return webClient;
    }

    public static class Cache {
        private long saveTimestamp;
        private JsonObject data;

        public Cache(long saveTimestamp, JsonObject data) {
            this.saveTimestamp = saveTimestamp;
            this.data = data;
        }

        public Future<Cache> save() {
            return Future.<Void>future(promise ->
                    vertx.fileSystem().writeFile(CONTEXT_CONFIG.cacheDir()
                            + "/" + saveTimestamp
                            + ".cache", data.toBuffer(), promise)
            ).compose(v ->
                    Future.succeededFuture(this)
            );
        }

        public long getSaveTimestamp() {
            return saveTimestamp;
        }

        public JsonObject getData() {
            return data;
        }
    }

    private String getClosestFileFromFilesByTimestamp(long timestamp, List<String> files) throws Exception {
        if(files.isEmpty())
            throw new Exception("Cache Dir Is Empty");

        Map<Long, String> timestampsToFileMap = new HashMap<>(files.size());
        List<Long> fileTimestamps = new ArrayList<>(files.size() + 1);
        for(String file: files){
            String fileName = fileName(file);
            Long fileTimestamp = Long.parseUnsignedLong(fileName);
            if(fileTimestamp == timestamp)
                return file;
            fileTimestamps.add(fileTimestamp);
            timestampsToFileMap.put(fileTimestamp, file);
        }
        fileTimestamps.add(timestamp);
        Collections.sort(fileTimestamps);
        int index = fileTimestamps.indexOf(timestamp);
        if(index == 0)
            return timestampsToFileMap.get(fileTimestamps.get(index + 1));
        else if(index == (fileTimestamps.size() - 1))
            return timestampsToFileMap.get(fileTimestamps.get(index - 1));
        else {
            long left = fileTimestamps.get(index - 1);
            long right = fileTimestamps.get(index + 1);
            long leftDifference = timestamp - left;
            long rightDifference = right - timestamp;
            if(leftDifference <= rightDifference)
                return timestampsToFileMap.get(left);
            else return timestampsToFileMap.get(right);
        }
    }

    private String fileName(String filePath) {
        String fileNameWithExtension = new File(filePath).getName();
        return fileNameWithExtension.substring(
                0,
                fileNameWithExtension.lastIndexOf(StrUtil.getFileExtension(fileNameWithExtension))
        );
    }
}
