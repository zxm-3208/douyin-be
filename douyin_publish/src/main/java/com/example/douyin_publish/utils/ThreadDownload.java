package com.example.douyin_publish.utils;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.Speed;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : zxm
 * @date: 2024/4/18 - 10:09
 * @Description: com.example.douyin_publish.utils
 * @version: 1.0
 */
@Slf4j
public class ThreadDownload extends Downer{

    // 线程下载数量，默认是1
    public static int threadCount=1;
    // 记录各个子线程是否下载完毕，下载完一个减去1
    public static int runningThread=1;
    // 文件总大小
    public volatile static int len=0;
    // 文件进度
    public volatile static int progress;

    SpeedListener sl;

    public ThreadDownload(String url_path, String save_path) {
        super(url_path, save_path);
    }

    public void setThreadCount(int threadCount){
        ThreadDownload.threadCount=threadCount;
        runningThread=threadCount;
    }

    @Override
    public void run() {
        RandomAccessFile raf = null;
        try {
            URL url = new URL(getUrl_path());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(100);
            conn.setRequestMethod("GET");
            if(conn.getResponseCode()==200){
                // 服务器返回的数据长度
                int length = conn.getContentLength();
                ThreadDownload.len=length;
                log.info("服务器返回的数据长度为{}",length);
                // 在本地创建一个大小跟服务器一样大小的临时文件
                raf = new RandomAccessFile(super.getSave_path(),"rwd");
                // 指定创建的这个文件长度
                raf.setLength(length);
                // 计算平均每个线程下载的文件大小
                int blockSize = length / threadCount;
                for(int i=1;i<=threadCount;i++){
                    // 第一个线程下载的开始位置
                    int startIndex = (i-1)*blockSize;
                    // 结束位置
                    int endIndex = i*blockSize-1;
                    // 最后一个线程结束位置是文件末尾
                    if(i==threadCount){
                        endIndex = length;
                    }
                    log.info("线程：{} 下载{}--->{}", i, startIndex, endIndex);
                    SonThreadDownload sonThreadDownload = new SonThreadDownload(getUrl_path(), getSave_path());
                    sonThreadDownload.setBpDownload(this.isBpDownload());
                    sonThreadDownload.setter(i, startIndex, endIndex);
//                    new Thread(sonThreadDownload).start();
                    // 使用线程池
                    ExecutorsPools.fixedThreadPool.execute(sonThreadDownload);
                }
                // 监听下载进度
                speed();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(raf!=null){
                try {
                    raf.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void speed() {
        int temp = 0;
        // 循环监控网速，如果下载进度达到100%就结束监控
        while (ThreadDownload.progress != ThreadDownload.len) {
            temp = progress;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // 当前下载进度除以文件总长得到下载进度
            double p = (double) temp / (double) len * 100;
            // 当前下载进度减去前一秒的下载进度就得到一秒内的下载速度
            temp = progress - temp;
            sl.speed(temp, p);
        }
        sl.speed(temp, 100);
        log.info("整个文件下载完毕了");
    }

    public void addSpeedListener(SpeedListener sl){
        this.sl = sl;
    }
}
