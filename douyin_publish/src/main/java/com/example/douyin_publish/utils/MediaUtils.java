package com.example.douyin_publish.utils;

import com.example.douyin_publish.domain.dto.UploadFileParamsDTO;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : zxm
 * @date: 2024/4/21 - 12:26
 * @Description: com.example.douyin_commons.utils
 * @version: 1.0
 */
@Slf4j
public class MediaUtils {
    // TODO: 清空临时文件
    public static Map<String, String> getScreenshot(String filePath) {
        try {
            Map<String, String> result = new HashMap<String, String>();
            FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(filePath);
            // 第一帧图片存储位置
            String targerFilePath = filePath.substring(0, filePath.lastIndexOf(File.separator));
            // 视频文件名
            String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
            // 图片名称
            String targetFileName = fileName.substring(0, fileName.lastIndexOf("."));
            log.info("视频路径是：{}", targerFilePath);
            log.info("视频文件名：{}", fileName);
            log.info("图片名称是：{}", targetFileName);

            grabber.start();
            //设置视频截取帧（默认取第一帧）
//            Frame frame = grabber.grabImage();
            int i = 0;
            int lenght = grabber.getLengthInFrames();
            log.info("----------------------------length={}",lenght);
            Frame f = null;
            while (i < lenght) {
                // 过滤前10帧，避免出现全黑的图片，依自己情况而定
                f=grabber.grabImage();
                if ((i > 10) && (f.image != null)) {
                    break;
                }
                i++;
            }
            Java2DFrameConverter converter = new Java2DFrameConverter();
            //绘制图片
            BufferedImage bi = converter.getBufferedImage(f);
            log.info("----------------------------------bi={}",bi);
            //图片的类型
            String imageMat = "jpg";
            log.info("---------------------------------targerFilePath={}",targerFilePath);
            log.info("---------------------------------File.separator={}",File.separator);
            log.info("---------------------------------targetFileName={}",targetFileName);
            log.info("---------------------------------imageMat={}",imageMat);
            //图片的完整路径
            String imagePath = targerFilePath + File.separator + targetFileName + "." + imageMat;
            log.info("---------------------------------imagePath={}",imagePath);
            //创建文件
            File output = new File(imagePath);
            ImageIO.write(bi, imageMat, output);
            log.info("-----------------------------------------output={}",output);
            log.info("----------------------------------------------output.getPath()={}",output.getPath());

            result.put("imgPath", output.getPath());
            grabber.stop();
            log.info("截取视频截图结束：{}", System.currentTimeMillis());
            return result;
        } catch (Exception e) {
            log.error("VideoUtil getScreenshot fail: {}", e);
            return null;
        }
    }
}
