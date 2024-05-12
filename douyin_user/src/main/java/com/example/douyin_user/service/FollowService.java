package com.example.douyin_user.service;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_user.domain.vo.AuthorFollowVo;

/**
 * @author : zxm
 * @date: 2024/5/12 - 13:06
 * @Description: com.example.douyin_user.service
 * @version: 1.0
 */
public interface FollowService {
    BaseResponse authorFollow(AuthorFollowVo authorFollowVo);

    BaseResponse isFollow(AuthorFollowVo authorFollowVo);
}
