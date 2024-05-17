package com.example.douyin_user.controller;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_user.domain.vo.GetListVo;
import com.example.douyin_user.service.FansListService;
import com.example.douyin_user.service.FollowListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author : zxm
 * @date: 2024/5/15 - 19:03
 * @Description: com.example.douyin_user.controller
 * @version: 1.0
 */
@RestController     //@Controller + @ResponseBody
@RequestMapping("/fansList")
@CrossOrigin
@Slf4j
public class FansListConrtoller {

    @Autowired
    private FansListService fansListService;

    @PostMapping("/getFansList")
    public BaseResponse getFollowList(@RequestBody GetListVo getListVo){
        return fansListService.getFansList(getListVo);
    }

    @PostMapping("/getOtherUserFansList")
    public BaseResponse getOtherUserFansList(@RequestBody GetListVo getListVo){
        return fansListService.getOtherUserFansList(getListVo);
    }

}
