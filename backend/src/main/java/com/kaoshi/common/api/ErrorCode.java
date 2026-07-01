package com.kaoshi.common.api;

public enum ErrorCode {
    VALIDATION_FAILED(40000, "请求参数不合法"),
    UNAUTHORIZED(40100, "登录状态无效"),
    FORBIDDEN(40300, "没有访问权限"),
    NOT_FOUND(40400, "资源不存在"),
    CONFLICT(40900, "资源状态冲突"),
    INTERNAL_ERROR(50000, "系统内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}

