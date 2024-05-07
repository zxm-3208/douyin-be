package com.example.douyin_user.controller;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_user.service.LikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author : zxm
 * @date: 2024/5/6 - 14:30
 * @Description: com.example.douyin_user.controller
 * @version: 1.0
 */
@RestController     //@Controller + @ResponseBody
@RequestMapping("/likes")
@CrossOrigin
@Slf4j
public class LikeController {

    @Autowired
    LikeService likeServiceImpl;

    @PutMapping("/mediaLike/{id}")
    public BaseResponse addLike(@PathVariable("id") Long id){
        return likeServiceImpl.addLike(id);
    }


}
