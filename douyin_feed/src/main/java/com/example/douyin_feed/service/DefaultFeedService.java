package com.example.douyin_feed.service;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_feed.domain.vo.ClickPlayVo;
import com.example.douyin_feed.domain.vo.MediaPlayVo;
import com.example.douyin_feed.domain.vo.UrlListVo;

/**
 * @author : zxm
 * @date: 2024/4/24 - 16:23
 * @Description: 按照发布时间排序
 * @version: 1.0
 */

public interface DefaultFeedService {

    BaseResponse getAllPublist(MediaPlayVo mediaPlayVo);

    BaseResponse getMediaPlay(UrlListVo urlListVo);

    BaseResponse clickPlayList(ClickPlayVo clickPlayVo);

    BaseResponse getUserPlay(ClickPlayVo clickPlayVo);
}
