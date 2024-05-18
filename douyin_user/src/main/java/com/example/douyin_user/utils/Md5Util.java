package com.example.douyin_user.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author : zxm
 * @date: 2024/5/18 - 11:43
 * @Description: com.example.douyin_user.utils
 * @version: 1.0
 */
public class Md5Util {

    /**
     * 计算字符串的MD5码。
     *
     * @param input 需要计算MD5码的字符串
     * @return 字符串的MD5码，16进制表示
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * 计算实体类的MD5值。
     *
     * @param obj 实体类对象
     * @return 对象的MD5值，16进制表示
     */
    public static String md5(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(obj); // 将对象转换为JSON字符串
            return md5(jsonString); // 调用之前的md5(String input)方法计算字符串的MD5
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute MD5 for object", e);
        }
    }

}
