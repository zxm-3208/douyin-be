package com.example.douyin_user.controller;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_user.domain.vo.AuthorFollowVo;
import com.example.douyin_user.service.EditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author : zxm
 * @date: 2024/5/13 - 16:40
 * @Description: com.example.douyin_user.controller
 * @version: 1.0
 */
@RestController     //@Controller + @ResponseBody
@RequestMapping("/edit")
@CrossOrigin
@Slf4j
public class EditController {

    @Autowired
    private EditService editService;

    @GetMapping("/getAttribute")
    public BaseResponse getAttribute(@RequestParam String userId){
        return editService.getAttribute(userId);
    }

}
