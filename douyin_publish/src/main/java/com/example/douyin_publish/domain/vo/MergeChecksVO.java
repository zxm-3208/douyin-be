package com.example.douyin_publish.domain.vo;

import lombok.Data;

/**
 * @author : zxm
 * @date: 2024/4/15 - 16:25
 * @Description: com.example.douyin_publish.domain.vo
 * @version: 1.0
 */
@Data
public class MergeChecksVO {
    private String fileMd5;
    private String fileName;
    private int chunkTotal;
}
