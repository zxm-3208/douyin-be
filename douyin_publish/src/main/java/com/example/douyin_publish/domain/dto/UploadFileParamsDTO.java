package com.example.douyin_publish.domain.dto;

import com.example.douyin_publish.domain.po.DyMedia;
import com.example.douyin_publish.domain.po.DyPublish;
import lombok.Data;

/**
 * @author : zxm
 * @date: 2024/4/9 - 12:12
 * @Description: com.example.douyin_publish.domain.dto
 * @version: 1.0
 */
@Data
public class UploadFileParamsDTO {


    private DyMedia dyMedia;

    private DyPublish dyPublish;

    /**
     * 文件content-type
     */
    private String contentType;

    /**
     *  文件大小
     */
    private Long fileSize;

}
