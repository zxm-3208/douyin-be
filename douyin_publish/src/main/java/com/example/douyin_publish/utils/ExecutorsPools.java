package com.example.douyin_publish.utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : zxm
 * @date: 2024/4/18 - 11:41
 * @Description: com.example.douyin_publish.utils
 * @version: 1.0
 */
public class ExecutorsPools {
    static final int MAX_T = 15;
    //创建固定线程数量的线程池
    public static final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(MAX_T);

}
