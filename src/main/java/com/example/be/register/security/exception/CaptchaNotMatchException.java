package com.example.be.register.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author : zxm
 * @date: 2024/3/27 - 11:56
 * @Description: com.example.be.register.security.exception
 * @version: 1.0
 */
public class CaptchaNotMatchException extends AuthenticationException {
    public CaptchaNotMatchException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public CaptchaNotMatchException(String msg) {
        super(msg);
    }
}
