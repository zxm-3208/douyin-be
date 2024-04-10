package com.example.douyin_publish.service;

import com.example.douyin_publish.domain.dto.UploadFileParamsDTO;
import com.example.douyin_publish.domain.dto.UploadFileResultDTO;

/**
 * @author : zxm
 * @date: 2024/4/9 - 12:24
 * @Description: com.example.douyin_publish.service
 * @version: 1.0
 */
public interface UploadService {
    public UploadFileResultDTO uploadFile(UploadFileParamsDTO uploadFileParamsDTO, byte[] bytes, String folder, String objectName);

    void addMediaFilesToDb(String fileMd5, UploadFileParamsDTO uploadFileParamsDTO, String objectName);
}
