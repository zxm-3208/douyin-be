package com.example.douyin_publish.utils;


import lombok.Data;

import java.io.File;

/**
 * @author : zxm
 * @date: 2024/4/18 - 9:59
 * @Description: 线程下载抽象类，增强了Runnable接口，主要增加了下载断点开关
 * @version: 1.0
 */
@Data
public abstract class Downer implements Runnable {

    private String url_path;
    private String save_path;
    // 下载进度保存位置
    private String temp_path="C:\\ProgramData\\";
    // 开启是否断点下载，为了保护磁盘，默认关闭
    public boolean bpDownload = false;

    public Downer(String url_path, String save_path) {
        this.url_path = url_path;
        this.save_path = save_path;
    }

    public String getFileName(){
        return new File(save_path).getName();
    }


}
