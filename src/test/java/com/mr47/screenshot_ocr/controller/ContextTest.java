package com.mr47.screenshot_ocr.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ContextTest {

    @Test
    void getClosestTimestampFromFiles() {
//        Context.prepare(new ContextConfig());
//        Context context = Context.context();
//        List<String> files = new ArrayList<>(10);
//        long time = System.currentTimeMillis();
//        for(int i = 0; i < 10; i++) {
//            long timeN = time + (i + 1) * 1024;
//            files.add(timeN + ".cache");
//        }
//        try {
//            String file = context.getClosestFileFromFilesByTimestamp(time, files);
//            assertEquals(file, files.get(0));
//        } catch (Exception e) {
//            log.error("test error", e);
//        }
//
//        files.clear();
//        for(int i = 0; i < 4; i++){
//            long timeN = time - (i + 1) * 1024;
//            files.add(timeN + ".cache");
//        }
//        long timeP = time + 1;
//        files.add(timeP + ".cache");
//        for(int i = 5; i < 10; i++){
//            long timeN = time + (i + 1) * 1024;
//            files.add(timeN + ".cache");
//        }
//        try {
//            String file = context.getClosestFileFromFilesByTimestamp(time, files);
//            assertEquals(files.get(4), file);
//        } catch (Exception e) {
//            log.error("test error", e);
//        }
//
//        files.clear();
//        for(int i = 9; i > -1; i--){
//            long timeN = time + (i + 1) * 1024;
//            files.add(timeN + ".cache");
//        }
//        try {
//            String file = context.getClosestFileFromFilesByTimestamp(time, files);
//            assertEquals(files.get(files.size() - 1), file);
//        } catch (Exception e) {
//            log.error("test error", e);
//        }
//
//        files.clear();
//        for(int i = 0; i < 4; i++){
//            long timeN = time - (i + 1) * 1024;
//            files.add(timeN + ".cache");
//        }
//        files.add(time + ".cache");
//        for(int i = 5; i < 10; i++){
//            long timeN = time + (i + 1) * 1024;
//            files.add(timeN + ".cache");
//        }
//        try {
//            String file = context.getClosestFileFromFilesByTimestamp(time, files);
//            assertEquals(file, time + ".cache");
//        } catch (Exception e) {
//            log.error("test error", e);
//        }
//
//        files.clear();
//        try {
//            context.getClosestFileFromFilesByTimestamp(time, files);
//        } catch (Exception e) {
//            assertEquals(e.getMessage(), "Cache Dir Is Empty");
//        }
    }
}