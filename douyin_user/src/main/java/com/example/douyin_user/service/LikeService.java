package com.example.douyin_user.service;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_user.domain.vo.AuthorFollowVo;
import com.example.douyin_user.domain.vo.VediaUserLikes;

/**
 * @author : zxm
 * @date: 2024/5/6 - 21:37
 * @Description: com.example.douyin_user.service.impl
 * @version: 1.0
 */
public interface LikeService {

    BaseResponse addLike(VediaUserLikes vediaUserLikes);

    BaseResponse getLikeCount(VediaUserLikes vediaUserLikes);

    BaseResponse initLikeFlag(VediaUserLikes vediaUserLikes);

    BaseResponse getUserLikeList(VediaUserLikes vediaUserLikes);

}
