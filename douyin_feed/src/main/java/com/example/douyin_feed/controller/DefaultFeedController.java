package com.example.douyin_feed.controller;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_feed.domain.vo.MediaPlayVo;
import com.example.douyin_feed.domain.vo.UrlListVo;
import com.example.douyin_feed.service.DefaultFeedService;
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
    DefaultFeedService defaultFeedService;

    @PostMapping("/getAllPublist")
    public BaseResponse getAllPublist(@RequestBody MediaPlayVo mediaPlayVo){
        return defaultFeedService.getAllPublist(mediaPlayVo);
    }

    @PostMapping("/getUrl")
    public BaseResponse getMediaPlay(@RequestBody UrlListVo urlListVo){
        return defaultFeedService.getMediaPlay(urlListVo);
    }
}
