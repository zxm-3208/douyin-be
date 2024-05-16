package com.example.douyin_user.service;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_user.domain.vo.AuthorFollowVo;
import com.example.douyin_user.domain.vo.GetListVo;

/**
 * @author : zxm
 * @date: 2024/5/15 - 19:04
 * @Description: com.example.douyin_user.service
 * @version: 1.0
 */
public interface FollowListService {

    BaseResponse getFollowList(GetListVo getListVo);

}
