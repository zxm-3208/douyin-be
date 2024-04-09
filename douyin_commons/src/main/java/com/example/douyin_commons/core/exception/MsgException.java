package com.example.douyin_commons.core.exception;

/**
 * @author : zxm
 * @date: 2024/4/9 - 12:53
 * @Description: com.example.douyin_commons.core.exception
 * @version: 1.0
 */
public class MsgException extends RuntimeException{

    private String errMessage;

    public MsgException() {
        super();
    }

    public MsgException(String message) {
        super(message);
        this.errMessage = message;
    }

    public String getErrMessage(){
        return errMessage;
    }

    public static void cast(String errMessage){
        throw new MsgException(errMessage);
    }
    public static void cast(CommonError commonError){
        throw new MsgException(commonError.getErrMessage());
    }

}
