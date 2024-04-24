package com.example.douyin_feed.controller;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_feed.domain.vo.MediaPlayVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author : zxm
 * @date: 2024/4/24 - 16:22
 * @Description: com.example.douyin_feed.controller
 * @version: 1.0
 */
@RestController     //@Controller + @ResponseBody
@RequestMapping("/defaultFeed")
@CrossOrigin
@Slf4j
public class DefaultFeedController {

    @Autowired
    DefaultFeedController defaultFeedController;

    @PostMapping("/getUrl")
    public BaseResponse getMediaPlay(@RequestBody MediaPlayVo mediaPlayVo){
        return defaultFeedController.getMediaPlay(mediaPlayVo);
    }
}
