package com.example.douyin_publish.service;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_publish.domain.vo.PublistVO;

/**
 * @author : zxm
 * @date: 2024/4/23 - 14:21
 * @Description: com.example.douyin_publish.service
 * @version: 1.0
 */
public interface ShowlistService {

    BaseResponse showPublist(PublistVO publistVO);
}
