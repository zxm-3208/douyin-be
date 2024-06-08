package com.example.douyin_chat_commons.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

/**
 * @author : zxm
 * @date: 2024/5/31 - 18:58
 * @Description: com.example.douyin_chat.util
 * @version: 1.0
 */
@Slf4j
public class IOUtil {

    public static String getHostAddress(){
        String ip = null;
        try{
            ip = InetAddress.getLocalHost().getHostAddress();
        }
        catch(Exception ex){
            log.error("获取ip报错", ex.getMessage());
        }
        return ip;
    }
}
