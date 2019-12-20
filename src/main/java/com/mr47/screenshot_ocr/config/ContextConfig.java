package com.mr47.screenshot_ocr.config;

import com.moandjiezana.toml.Toml;

import java.util.concurrent.TimeUnit;

public class ContextConfig implements TomlConfig {
    private String cacheDir;
    private String outputDir;
    private int concurrentCallNumber;
    private int callTimeOut;
    private TimeUnit callTimeOutUnit;
    private int imageQueueSize;
    private EventLoop eventLoop;
    private BaiduAI baiduAI;

    public ContextConfig() {
        outputDir = "output";
        cacheDir = "cache";
        concurrentCallNumber = 5;
        callTimeOut = 60000;
        callTimeOutUnit = TimeUnit.MILLISECONDS;
        imageQueueSize = 200;
        eventLoop = new EventLoop();
        baiduAI = new BaiduAI();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ContextConfig mergeFromToml(Toml toml) {
        if(toml.contains("output"))
            outputDir = toml.getString("output");

        if(toml.contains("cache"))
            cacheDir = toml.getString("cache");

        if(toml.contains("concurrent_call_number"))
            concurrentCallNumber = toml.getLong("concurrent_call_number")
                    .intValue();

        if(toml.contains("call_timeout"))
            callTimeOut = toml.getLong("call_timeout")
                    .intValue();

        if(toml.contains("call_timeout_unit"))
            switch (toml.getString("call_timeout_unit")) {
                case "millisecond": { callTimeOutUnit = TimeUnit.MILLISECONDS;break; }
                case "second": { callTimeOutUnit = TimeUnit.SECONDS;break; }
                case "minute": { callTimeOutUnit = TimeUnit.MINUTES;break; }
                default: throw new IllegalArgumentException("Unknown Time Unit Type: "
                        + toml.getString("call_timeout_unit"));
            }

        if(toml.contains("image_queue_size"))
            imageQueueSize = toml.getLong("image_queue_size")
                    .intValue();

        if(toml.containsTable("event_loop"))
            eventLoop.mergeFromToml(toml.getTable("event_loop"));

        if(toml.containsTable("baidu_ai"))
            baiduAI.mergeFromToml(toml.getTable("baidu_ai"));

        return this;
    }

    public EventLoop eventLoop() {
        return eventLoop;
    }

    public BaiduAI baiduAI() {
        return baiduAI;
    }

    public String outputDir() {
        return outputDir;
    }

    public String cacheDir() {
        return cacheDir;
    }

    public int concurrentCallNumber() {
        return concurrentCallNumber;
    }

    public int callTimeOut() {
        return callTimeOut;
    }

    public TimeUnit callTimeOutUnit() {
        return callTimeOutUnit;
    }

    public int imageQueueSize() {
        return imageQueueSize;
    }

    public static class EventLoop implements TomlConfig {
        private int size;
        private long blockedCheckInterval;
        private TimeUnit blockedCheckIntervalUnit;

        private EventLoop() {
            size = 1;
            blockedCheckInterval = 1000L;
            blockedCheckIntervalUnit = TimeUnit.MILLISECONDS;
        }

        @SuppressWarnings("unchecked")
        @Override
        public EventLoop mergeFromToml(Toml toml) {
            if(toml.contains("size"))
                size = toml.getLong("size").intValue();

            if(toml.contains("blocked_check_interval"))
                blockedCheckInterval = toml.getLong("blocked_check_interval");

            if(toml.contains("blocked_check_interval_unit"))
                switch (toml.getString("blocked_check_interval_unit")) {
                    case "microsecond": { blockedCheckIntervalUnit = TimeUnit.MICROSECONDS;break; }
                    case "millisecond": { blockedCheckIntervalUnit = TimeUnit.MILLISECONDS;break; }
                    case "second": { blockedCheckIntervalUnit = TimeUnit.SECONDS;break; }
                    case "minute": { blockedCheckIntervalUnit = TimeUnit.MINUTES;break; }
                    default: throw new IllegalArgumentException("Unknown Time Unit Type: "
                            + toml.getString("blocked_check_interval_unit"));
                }

            return this;
        }

        public int size() {
            return size;
        }

        public long blockedCheckInterval() {
            return blockedCheckInterval;
        }

        public TimeUnit blockedCheckIntervalUnit() {
            return blockedCheckIntervalUnit;
        }
    }

    public static class BaiduAI implements TomlConfig {
        private String apiKey;
        private String secretKey;
        private boolean autoRefreshToken;
        private long refreshTokenInterval;
        private TimeUnit refreshTokenIntervalUnit;

        private BaiduAI() {
            apiKey = "j7kaw8MLF7XVXVtiUkWm7la7";
            secretKey = "suIqlxnG5V5DQmcqxuduPzLjDsjrbRxM";
            autoRefreshToken = true;
            refreshTokenInterval = 2L;
            refreshTokenIntervalUnit = TimeUnit.DAYS;
        }

        @SuppressWarnings("unchecked")
        @Override
        public BaiduAI mergeFromToml(Toml toml) {
            if(toml.contains("api_key"))
                apiKey = toml.getString("api_key");

            if(toml.contains("secret_key"))
                secretKey = toml.getString("secret_key");

            if(toml.contains("auto_refresh_token"))
                autoRefreshToken = toml.getBoolean("auto_refresh_token");

            if(toml.contains("refresh_token_interval"))
                refreshTokenInterval = toml.getLong("refresh_token_interval");

            if(toml.contains("refresh_token_interval_unit"))
                switch (toml.getString("refresh_token_interval_unit")) {
                    case "second": { refreshTokenIntervalUnit = TimeUnit.SECONDS;break; }
                    case "minute": { refreshTokenIntervalUnit = TimeUnit.MINUTES;break; }
                    case "hour": { refreshTokenIntervalUnit = TimeUnit.HOURS;break; }
                    case "day": { refreshTokenIntervalUnit = TimeUnit.DAYS;break; }
                    default: throw new IllegalArgumentException("Unknown Time Unit Type: "
                            + toml.getString("refresh_token_interval_unit"));
                }

            return this;
        }

        public String apiKey() {
            return apiKey;
        }

        public String secretKey() {
            return secretKey;
        }

        public boolean autoRefreshToken() {
            return autoRefreshToken;
        }

        public long refreshTokenInterval() {
            return refreshTokenInterval;
        }

        public TimeUnit refreshTokenIntervalUnit() {
            return refreshTokenIntervalUnit;
        }
    }
}
