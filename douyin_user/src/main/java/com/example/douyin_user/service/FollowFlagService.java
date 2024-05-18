package com.example.douyin_user.service;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_user.domain.vo.GetListVo;

/**
 * @author : zxm
 * @date: 2024/5/17 - 22:07
 * @Description: com.example.douyin_user.service
 * @version: 1.0
 */
public interface FollowFlagService {
    BaseResponse getFollowFlag(GetListVo getListVo);
}
