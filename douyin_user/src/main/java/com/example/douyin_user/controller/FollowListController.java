package com.example.douyin_user.controller;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_user.domain.vo.AuthorFollowVo;
import com.example.douyin_user.domain.vo.GetListVo;
import com.example.douyin_user.service.FollowListService;
import com.example.douyin_user.service.FollowService;
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
@RequestMapping("/followList")
@CrossOrigin
@Slf4j
public class FollowListController {

    @Autowired
    private FollowListService followListService;

    @PostMapping("/getFollowList")
    public BaseResponse getFollowList(@RequestBody GetListVo getListVo){
        return followListService.getFollowList(getListVo);
    }


}
