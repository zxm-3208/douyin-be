package com.example.douyin_commons.core.domain;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author : zxm
 * @date: 2024/3/19 - 21:31
 * @Description: com.example.be.common.core.domain
 * @version: 1.0
 */
public class BaseResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = -5958420328797524626L;

    public BaseResponse() {
    }

    public BaseResponse(int code, T data) {
        this.code = code;
        this.data = data;
    }

    /**
     * 响应状态码
     */
    private int code;

    /**
     * 响应结果描述
     */
    private String msg;

    /**
     * 返回的数据
     */
    private T data;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * @description: 成功返回
     * @param: data
     * @return: com.example.be.common.core.domain.BaseResponse<T>
     * @author zxm
     * @date: 2024/3/19 21:43
     */
    public static <T> BaseResponse<T> success(T data){
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(ResultCode.SUCCESS.getCode());
        response.setMsg(ResultCode.SUCCESS.getMessage());
        response.setData(data);
        response.setSuccess(true);
        return response;
    }

    /**
     * @description: 成功返回
     * @return: com.example.be.common.core.domain.BaseResponse<T>
     * @author zxm
     * @date: 2024/3/19 22:55
     */
    public static <T> BaseResponse<T> success(){
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(ResultCode.SUCCESS.getCode());
        response.setMsg(ResultCode.SUCCESS.getMessage());
        response.setSuccess(true);
        return response;
    }

    /** 
     * @description: 失败返回
     * @param: message 
     * @return: com.example.be.common.core.domain.BaseResponse<T> 
     * @author zxm
     * @date: 2024/3/19 22:03
     */ 
    public static <T> BaseResponse<T> fail(String message){
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(ResultCode.Error.getCode());
        response.setMsg(message);
        response.setSuccess(false);
        return response;
    }

    /** 
     * @description:  
     * @param code 
     * @param message  
     * @return: com.example.be.common.core.domain.BaseResponse<T>
     * @author zxm
     * @date: 2024/3/19 22:13
     */ 
    public static <T> BaseResponse<T> fail(int code, String message){
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(code);
        response.setMsg(message);
        response.setSuccess(false);
        return response;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
