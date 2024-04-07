package com.example.douyin_commons.core.domain;


import com.example.douyin_commons.constant.HttpStatus;

/**
 * @author : zxm
 * @date: 2024/3/19 - 21:43
 * @Description: 响应状态码枚举
 * @version: 1.0
 */
public enum ResultCode {

    SUCCESS(HttpStatus.SUCCESS, "操作成功"),
    Error(HttpStatus.ERROR, "操作失败");

    private int code;

    private String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
