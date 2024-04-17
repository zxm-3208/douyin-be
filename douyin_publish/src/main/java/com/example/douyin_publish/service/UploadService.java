package com.example.douyin_publish.service;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.domain.ResultCode;
import com.example.douyin_publish.domain.dto.UploadFileParamsDTO;
import com.example.douyin_publish.domain.dto.UploadFileResultDTO;

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

}
