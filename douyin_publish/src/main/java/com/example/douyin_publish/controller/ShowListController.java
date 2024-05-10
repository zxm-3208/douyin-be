package com.example.douyin_publish.controller;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_publish.domain.vo.PublistVO;
import com.example.douyin_publish.service.ShowlistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author : zxm
 * @date: 2024/4/23 - 14:22
 * @Description: com.example.douyin_publish.controller
 * @version: 1.0
 */
@RestController
@CrossOrigin
@RequestMapping("/showlist")
@Slf4j
public class ShowListController {

    @Autowired
    ShowlistService showlistService;

    @PostMapping("/publist")
    public BaseResponse showPublist(@RequestBody PublistVO publistVO){
        return showlistService.showPublist(publistVO);
    }

    @PostMapping("/likeList")
    public BaseResponse showLikeList(@RequestBody PublistVO publistVO){
        return showlistService.showLikeList(publistVO);
    }


}
