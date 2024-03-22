package com.example.be.common.utils;

import java.util.UUID;

/**
 * @author : zxm
 * @date: 2024/3/22 - 10:33
 * @Description: UUID生成器工具类
 * @version: 1.0
 */
public class UUIDUtils {

    public static String randomUUID(){
        return UUID.randomUUID().toString();
    }

    public static String simpleUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
