package com.example.douyin_user.controller;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_user.domain.vo.AuthorFollowVo;
import com.example.douyin_user.domain.vo.VediaUserLikes;
import com.example.douyin_user.service.FollowService;
import com.example.douyin_user.service.LikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author : zxm
 * @date: 2024/5/12 - 13:05
 * @Description: com.example.douyin_user.controller
 * @version: 1.0
 */
@RestController     //@Controller + @ResponseBody
@RequestMapping("/follow")
@CrossOrigin
@Slf4j
public class FollowController {

    @Autowired
    FollowService followService;

    @PostMapping("/authorFollow")
    public BaseResponse authorFollow(@RequestBody AuthorFollowVo authorFollowVo){
        return followService.authorFollow(authorFollowVo);
    }

    @PostMapping("/isFollow")
    public BaseResponse isFollow(@RequestBody AuthorFollowVo authorFollowVo){
        return followService.isFollow(authorFollowVo);
    }

    @GetMapping("/getFollowCount")
    public BaseResponse getFollowCount(@RequestParam String userId){
        return followService.getFollowCount(userId);
    }

    @GetMapping("/getFansCount")
    public BaseResponse getFansCount(@RequestParam String userId){
        return followService.getFansCount(userId);
    }

}
