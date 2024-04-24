package com.example.douyin_publish.service;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_publish.domain.dto.UploadFileParamsDTO;
import com.example.douyin_publish.domain.dto.UploadFileResultDTO;
import com.example.douyin_publish.domain.vo.EditVo;

import java.io.File;

/**
 * @author : zxm
 * @date: 2024/4/9 - 12:24
 * @Description: com.example.douyin_publish.service
 * @version: 1.0
 */
public interface UploadService {
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
    UploadFileResultDTO uploadFile(UploadFileParamsDTO uploadFileParamsDTO, byte[] bytes, String folder, String objectName);

    /**
     * @description: 上传切片
     * @param uploadFileParamsDTO
     * @param bytes
     * @return: com.example.douyin_publish.domain.dto.UploadFileResultDTO
     * @author zxm
     * @date: 2024/4/15 13:45
     */
    UploadFileResultDTO uploadChunk(UploadFileParamsDTO uploadFileParamsDTO,byte[] bytes);

    /**
     * @description: 合并切片
     * @param uploadFileParamsDTO
     * @return: com.example.douyin_publish.domain.dto.UploadFileResultDTO
     * @author zxm
     * @date: 2024/4/15 13:45
     */
    UploadFileResultDTO mergeChunk(UploadFileParamsDTO uploadFileParamsDTO);

    boolean addMediaFilesToDb(UploadFileParamsDTO uploadFileParamsDTO, String objectName);

    /**
     * @description: 判断文件是否上传过
     * @param fileMd5
     * @return: 1:文件已存在，0：文件没有上传过，2：文件上传且中断过，以及现在有的数组分片索引
     * @author zxm
     * @date: 2024/4/12 10:20
     */
    BaseResponse checkFile(String fileMd5);

    /**
     * @description: 根据桶和文件路径从minio下载文件
     * @param file
     * @param bucket
     * @param objectName
     * @return: void
     * @author zxm
     * @date: 2024/4/15 13:23
     */
    void downloadFileFromMinIO(File file, String bucket, String objectName);

    void countDown();

    /** 
     * @description: 生成视频外链
     * @param MD5
     * @return: com.example.douyin_commons.core.domain.BaseResponse 
     * @author zxm
     * @date: 2024/4/19 15:03
     */ 
    BaseResponse downloadCreative(String MD5);

    /**
     * @description: 生成封面外链
     * @param mediaId
     * @return: com.example.douyin_commons.core.domain.BaseResponse
     * @author zxm
     * @date: 2024/4/21 14:04
     */
    BaseResponse downloadCover(String mediaId);

    /**
     * @description: 封面图片上传(字节流)
     * @param uploadFileParamsDTO
     * @param bytes
     * @return: com.example.douyin_publish.domain.dto.UploadFileResultDTO
     * @author zxm
     * @date: 2024/4/20 21:51
     */
    UploadFileResultDTO uploadCoverFile(UploadFileParamsDTO uploadFileParamsDTO, byte[] bytes);

    /**
     * @description: 封面图片上传(路径)
     * @param uploadFileParamsDTO
     * @param filePath
     * @return: null
     * @author zxm
     * @date: 2024/4/21 13:08
     */
    void uploadCoverFile(UploadFileParamsDTO uploadFileParamsDTO, String filePath);

    Boolean addCoverFilesToDb(String fileId, String objectName);

    UploadFileResultDTO editPublist(EditVo editVo);

}
