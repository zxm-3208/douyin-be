package com.example.douyin_feed.controller;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_feed.domain.po.DyMedia;
import com.example.douyin_feed.domain.vo.ClickPlayVo;
import com.example.douyin_feed.domain.vo.MediaPlayVo;
import com.example.douyin_feed.domain.vo.UrlListVo;
import com.example.douyin_feed.service.DefaultFeedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public BaseResponse getAllPublist(){
        return defaultFeedService.getAllPublist();
    }

    @PostMapping("/getUrl")
    public BaseResponse getMediaPlay(@RequestBody UrlListVo urlListVo){
        return defaultFeedService.getMediaPlay(urlListVo);
    }

    @PostMapping("/getUserUrl")
    public BaseResponse getUserPlay(@RequestBody ClickPlayVo clickPlayVo){
        return defaultFeedService.getUserPlay(clickPlayVo);
    }

    @PostMapping("/clickPlay")
    public BaseResponse getClickPlay(@RequestBody ClickPlayVo clickPlayVo){
        return defaultFeedService.clickPlayList(clickPlayVo);
    }

    @PostMapping("/getUserLikeUrl")
    public BaseResponse getUserLikePlay(@RequestBody ClickPlayVo clickPlayVo){
        return defaultFeedService.getUserLikePlay(clickPlayVo);
    }

}
