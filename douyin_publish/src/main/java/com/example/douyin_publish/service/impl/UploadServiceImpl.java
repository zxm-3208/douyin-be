package com.example.douyin_publish.service.impl;

import com.example.douyin_commons.core.exception.MsgException;
import com.example.douyin_publish.domain.dto.UploadFileParamsDTO;
import com.example.douyin_publish.domain.dto.UploadFileResultDTO;
import com.example.douyin_publish.domain.po.DyMedia;
import com.example.douyin_publish.domain.po.DyPublish;
import com.example.douyin_publish.mapper.MediaFilesMapper;
import com.example.douyin_publish.mapper.PublishMapper;
import com.example.douyin_publish.service.UploadService;
import io.micrometer.common.util.StringUtils;
import com.example.douyin_publish.config.MinioConfig;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * @author : zxm
 * @date: 2024/4/9 - 12:26
 * @Description: com.example.douyin_publish.service.impl
 * @version: 1.0
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    PublishMapper publishMapper;

//    @Autowired
//    UploadService currentProxy;

    // 普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_Files;

    private DyMedia dyMedia;
    private DyPublish dyPublish;



    /**
     * @description:  上传文件
     * @param uploadFileParamsDTO 上传文件的信息
     * @param bytes 文件比特
     * @param folder 文件名
     * @param objectName
     * @return: com.example.douyin_publish.domain.dto.UploadFileResultDTO
     * @author zxm
     * @date: 2024/4/9 15:23
     */
    @Override
    public UploadFileResultDTO uploadFile(UploadFileParamsDTO uploadFileParamsDTO, byte[] bytes, String folder, String objectName) {

        // 生成文件id, 文件的md5值
        String fileId = DigestUtils.md5Hex(bytes);
        // 文件名称
        String filename = uploadFileParamsDTO.getDyPublish().getTitle();
        // 构造objectname
        if(StringUtils.isEmpty(objectName)){
            objectName = fileId + filename.substring(filename.lastIndexOf("."));
        }
        // 通过日期构造文件存储路径
        if(StringUtils.isEmpty(folder)){
            folder = getFileFolder(new Date(), true, true, true);
        } else if (folder.indexOf("/")<0) {
            folder = folder + "/";
        }
        // 对象名称
        objectName = folder + objectName;

        try{
            // 上传至文件系统
            addMediaFilesToMinIO(bytes, objectName, uploadFileParamsDTO.getContentType());
            // 写入数据库表
            getService().addMediaFilesToDb(fileId, uploadFileParamsDTO, objectName);
            UploadFileResultDTO uploadFileResultDTO = new UploadFileResultDTO();
            BeanUtils.copyProperties(dyMedia, uploadFileResultDTO);
            BeanUtils.copyProperties(dyPublish, uploadFileResultDTO);
            return uploadFileResultDTO;
        }catch (Exception e){
            e.printStackTrace();
            MsgException.cast("上传过程中出错");
        }
        return null;
    }

    /**
     * @description: 将文件写入minIO
     * @param bytes 文件字节数组
     * @param bucket 桶
     * @param objectName 对象名称
     * @param contentType 内容类型 （即是Internet Media Type，互联网媒体类型，也叫做MIME类型，通过这个属性来告诉服务器如何处理请求的数据。）
     * @return: void
     * @author zxm
     * @date: 2024/4/9 15:25
     */
    private void addMediaFilesToMinIO(byte[] bytes, String objectName, String contentType) {
        // 转为流
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucket_Files)
                    .object(objectName)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1) // -1表示文件分片按(不小于5M，不大于5T),分片数量最大10000
                    .contentType(contentType)
                    .build();
            minioClient.putObject(putObjectArgs);
        }catch (Exception e){
            e.printStackTrace();
            MsgException.cast("上传文件到文件系统出错");
        }
    }

    /**
     * @description: 将文件信息添加到文件夹
     * @param fileMd5 文件md5值
     * @param uploadFileParamsDTO 上传文件的信息
     * @param objectName  对象名称
     * @return: com.example.douyin_publish.domain.po.DyMedia
     * @author zxm
     * @date: 2024/4/9 15:44
     */
    @Transactional
    public void addMediaFilesToDb(String fileMd5, UploadFileParamsDTO uploadFileParamsDTO, String objectName) {
        // 从数据库查询文件
        dyMedia = mediaFilesMapper.selectById(fileMd5);
        System.out.println("=====");
        System.out.println(dyMedia);
        dyPublish = publishMapper.selectByMediaId(fileMd5);
        if(dyMedia == null){
            dyMedia = new DyMedia();
            // 拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDTO.getDyMedia(), dyMedia);
            // TODO: 补充dyMdia的额外信息
            //保存文件信息到DyMedia表
            int insert = mediaFilesMapper.insert(dyMedia);
            if (insert < 0) {
                MsgException.cast("保存文件信息失败");
            }
        }
        if(dyPublish == null){
            dyPublish = new DyPublish();
            // 拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDTO.getDyPublish(), dyPublish);
            // TODO: 补充dyPublish的额外信息
            //保存文件信息到DyPublish表
            int insert = publishMapper.insert(dyPublish);
            if (insert < 0) {
                MsgException.cast("保存文件信息失败");
            }
        }
    }

    private String getFileFolder(Date date, boolean year, boolean month, boolean day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 获取当前日期字符串
        String dateString = sdf.format(date);
        // 取出年、月、日
        String[] dateStringArray = dateString.split("-");
        StringBuffer folderString = new StringBuffer();
        if(year){
            folderString.append(dateStringArray[0]);
            folderString.append("/");
        }
        if(month){
            folderString.append(dateStringArray[1]);
            folderString.append("/");
        }
        if(year){
            folderString.append(dateStringArray[2]);
            folderString.append("/");
        }
        return folderString.toString();
    }

    /**
     *  通过AopContext获取代理类
     */
    private UploadService getService(){
        return Objects.nonNull(AopContext.currentProxy()) ? (UploadService) AopContext.currentProxy() : this;
    }
}
