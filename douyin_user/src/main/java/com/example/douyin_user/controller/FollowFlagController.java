package com.example.douyin_user.controller;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_user.domain.vo.GetListVo;
import com.example.douyin_user.service.FollowFlagService;
import com.example.douyin_user.service.FollowListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author : zxm
 * @date: 2024/5/17 - 22:07
 * @Description: com.example.douyin_user.controller
 * @version: 1.0
 */
@RestController     //@Controller + @ResponseBody
@RequestMapping("/followFlag")
@CrossOrigin
@Slf4j
public class FollowFlagController {

    @Autowired
    private FollowFlagService followFlagService;

    @PostMapping("/getFollowFlag")
    public BaseResponse getFollowFlag(@RequestBody GetListVo getListVo){
        return followFlagService.getFollowFlag(getListVo);
    }

}
