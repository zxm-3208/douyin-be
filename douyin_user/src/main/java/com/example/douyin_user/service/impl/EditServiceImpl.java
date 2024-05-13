package com.example.douyin_user.service.impl;

import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.domain.EditDTO;
import com.example.douyin_commons.core.domain.ResultCode;
import com.example.douyin_user.domain.po.dbAuth.DyUser;
import com.example.douyin_user.mapper.master.DyUserMapper;
import com.example.douyin_user.service.EditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : zxm
 * @date: 2024/5/13 - 16:41
 * @Description: com.example.douyin_user.service.impl
 * @version: 1.0
 */
@Service
@Slf4j
public class EditServiceImpl implements EditService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DyUserMapper dyUserMapper;

    @Override
    public BaseResponse getAttribute(String userId) {
        String key = RedisConstants.USER_EDIT_KEY + userId;
        Long size = redisTemplate.opsForHash().size(key);
        Map map = new HashMap<String, String>();
        if(size.equals(0L)){
            DyUser edit = dyUserMapper.getEdit(userId);
            if(edit!=null){
                log.info("查询用户信息成功:{}",edit);
                map.put("icon", edit.getIcon());
                map.put("editName", edit.getUserName());
                map.put("userId", edit.getId());
                map.put("editIntro", edit.getIntroduction());
                map.put("gender", edit.getSex());
                map.put("birthday", String.valueOf(edit.getBirthday().getTime()));
                log.info("time:{}", edit.getBirthday().getTime());
                redisTemplate.opsForHash().putAll(key, map);
            }
        }
        else{
            map = redisTemplate.opsForHash().entries(key);
        }
        return BaseResponse.success(map);
    }
}
