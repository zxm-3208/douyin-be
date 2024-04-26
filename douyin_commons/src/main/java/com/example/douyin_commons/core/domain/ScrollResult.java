package com.example.douyin_commons.core.domain;

import lombok.Data;

import java.util.List;

/**
 * @author : zxm
 * @date: 2024/4/26 - 10:49
 * @Description: com.example.douyin_commons.core.domain
 * @version: 1.0
 */
@Data
public class ScrollResult {
    private List<?> list;
    private Long minTIme;
    private Integer offset;
}
