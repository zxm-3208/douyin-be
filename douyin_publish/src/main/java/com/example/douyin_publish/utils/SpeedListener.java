package com.example.douyin_publish.utils;

/**
 * @author : zxm
 * @date: 2024/4/18 - 12:02
 * @Description: com.example.douyin_publish.utils
 * @version: 1.0
 */
public interface SpeedListener {
    /**
     * s: 当前下载进度，单位字节
     * progress: 下载进度，百分比
     */
    void speed(int s, double progress);
}
