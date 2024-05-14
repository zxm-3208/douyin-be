package com.example.douyin_user.service;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_user.domain.vo.SubmitEditVO;

/**
 * @author : zxm
 * @date: 2024/5/13 - 16:41
 * @Description: com.example.douyin_user.service
 * @version: 1.0
 */
public interface EditService {
    BaseResponse getAttribute(String userId);

    BaseResponse submitEdit(SubmitEditVO submitEditVO);
}
