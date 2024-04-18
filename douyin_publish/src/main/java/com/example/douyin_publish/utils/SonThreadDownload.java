package com.example.douyin_publish.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author : zxm
 * @date: 2024/4/18 - 10:35
 * @Description: com.example.douyin_publish.utils
 * @version: 1.0
 */
@Slf4j
public class SonThreadDownload extends Downer{

    private int threadId;
    private int startIndex;
    private int endIndex;

    public SonThreadDownload(String url_path, String save_path){
        super(url_path, save_path);
    }

    public void setter(int threadId, int startIndex, int endIndex){
        this.threadId = threadId;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        RandomAccessFile raf = null;
        FileInputStream fis = null;
        RandomAccessFile info = null;
        try{
            // 检查是否存在记录下载长度的文件，如果存在就读取这个文件的数据
            File tempFile = new File(getTemp_path() + getFileName() + threadId + ".temp");
            // 检查是否开启断点继续下载
            if(this.isBpDownload()&&tempFile.exists()&&tempFile.length()>0){
                fis = new FileInputStream(tempFile);
                byte[] temp = new byte[1024];
                int len = fis.read(temp);
                String s = new String(temp, 0, len);
                int downloadLenInt = Integer.parseInt(s) - 1;
                // 修改下载开始的位置
                startIndex = downloadLenInt;
            }
            URL url = new URL(getUrl_path());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(100);
            conn.setRequestMethod("GET");
            // 请求服务器下载部分文件的指定位置
            conn.setRequestProperty("Range","bytes="+startIndex+"-"+endIndex);
            // 请求服务器全部资源(200)，请求部分资源(206)
            int responseCode = conn.getResponseCode();
            if(responseCode==206){
                raf=new RandomAccessFile(getSave_path(), "rwd");
                inputStream=conn.getInputStream();
                // 定位文件从哪个位置开始写
                raf.seek(startIndex);
                int len=0;
                byte[] buff=new byte[1024*1024];
                // 已经下载的数据长度
                int total=0;

                while((len=inputStream.read(buff))!=-1){
                    raf.write(buff, 0, len);
                    synchronized (ThreadDownload.class){
                        ThreadDownload.progress += len;
                    }
                    total+=len;
                    if(isBpDownload()){
                        // 以文件名+线程id保存为临时文件，保存当前线程的下载进度
                        info = new RandomAccessFile(getTemp_path() + getFileName() + threadId + ".temp", "rwd");
                        info.write(String.valueOf(total + startIndex).getBytes());

                    }
                }
            }
            log.info("线程:{}号下载完毕");
            if(isBpDownload()){
                synchronized (ThreadDownload.class){
                    // 下载中的线程--，当减到0的时候代表整个文件下载完毕，如果中途异常，那么这个文件就没下载完
                    ThreadDownload.runningThread--;
                    if(ThreadDownload.runningThread==0){
                        for(int i=0;i<ThreadDownload.threadCount;i++){
                            File file = new File(getTemp_path() + getFileName() + threadId + ".temp", "rwd");
                            file.delete();
                            log.info("下载完毕，清除临时文件");
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("线程：{}号出错", threadId);
        }finally {
            if(raf!=null){
                try {
                    raf.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(inputStream!=null){
                try {
                    inputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(fis!=null){
                try{
                    fis.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(info!=null){
                try{
                    info.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
