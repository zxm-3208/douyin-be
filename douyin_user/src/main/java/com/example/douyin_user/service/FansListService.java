package com.example.douyin_user.service;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_user.domain.vo.GetListVo;

/**
 * @author : zxm
 * @date: 2024/5/15 - 19:05
 * @Description: com.example.douyin_user.service
 * @version: 1.0
 */
public interface FansListService {

    BaseResponse getFansList(GetListVo getListVo);

}
