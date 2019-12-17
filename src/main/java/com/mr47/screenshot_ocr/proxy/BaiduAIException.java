package com.mr47.screenshot_ocr.proxy;

public class BaiduAIException extends Exception {
    private int errorCode;
    private String cnMsg;
    private boolean needCnMsg;

    public BaiduAIException(int errorCode, String message, String cnMessage) {
        super(message);
        this.errorCode = errorCode;
        this.cnMsg = cnMessage;
        this.needCnMsg = false;
    }

    public BaiduAIException useCn() {
        needCnMsg = true;
        return this;
    }

    public BaiduAIException useEn() {
        needCnMsg = false;
        return this;
    }

    @Override
    public String getMessage() {
        if(needCnMsg) return cnMsg;
        else return super.getMessage();
    }

    public int errorCode() {
        return errorCode;
    }
}
