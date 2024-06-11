package com.example.douyin_chat_gate.service.impl;

import com.example.douyin_chat_commons.domain.DTO.ChatUserDTO;
import com.example.douyin_chat_commons.domain.po.DyUser;
import com.example.douyin_chat_commons.domain.po.ImNode;
import com.example.douyin_chat_commons.domain.po.LoginBack;
import com.example.douyin_chat_commons.domain.vo.BackVo;
import com.example.douyin_chat_commons.domain.vo.ChatUserVo;
import com.example.douyin_chat_commons.domain.vo.SendChat;
import com.example.douyin_chat_commons.util.JsonUtil;
import com.example.douyin_chat_gate.balance.ImLoadBalance;
import com.example.douyin_chat_gate.mapper.ChatMapper;
import com.example.douyin_chat_gate.service.ChatOpenFeignService;
import com.example.douyin_chat_gate.service.ChatOpenFeignService2;
import com.example.douyin_chat_gate.service.GateService;
import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.domain.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : zxm
 * @date: 2024/6/9 - 17:53
 * @Description: com.example.douyin_chat_gate.service.impl
 * @version: 1.0
 */
@Service
@Slf4j
public class GateServiceImpl implements GateService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private ImLoadBalance imLoadBalance;

    @Autowired
    private ChatOpenFeignService chatOpenFeignService;

    @Autowired
    private ChatOpenFeignService2 chatOpenFeignService2;

    @Override
    public BaseResponse login(ChatUserVo chatUserVO) {
        log.info("step1: 开始登录WEB GATE");
        // 1. 查询userId是否在redis中
        log.info("userId:{}", chatUserVO.getUserId());
        Long num = redisTemplate.opsForValue().size(RedisConstants.CHAT_USER_LOGIN_KEY + chatUserVO.getUserId());
        // 2. 如果不在则在mysql中查询
        DyUser dyUser = null;
        if(num==0){
            dyUser = chatMapper.selectByUserId(chatUserVO.getUserId());
            redisTemplate.opsForValue().set(RedisConstants.CHAT_USER_LOGIN_KEY + chatUserVO.getUserId(), dyUser);
        }else{
            dyUser = (DyUser) redisTemplate.opsForValue().get(RedisConstants.CHAT_USER_LOGIN_KEY + chatUserVO.getUserId());
            if(dyUser == null){
                log.error("用户不存在");
                return BaseResponse.fail("用户不存在");
            }
        }
        LoginBack back = new LoginBack();

        // 3. 取得所有节点
        List<ImNode> allWorker = imLoadBalance.getWorkers();
        back.setImNodeList(allWorker);
        ChatUserDTO userDTO = new ChatUserDTO();
        BeanUtils.copyProperties(chatUserVO, userDTO);
        userDTO.setToken(chatUserVO.getToken());
        back.setUserDTO(userDTO);
        back.setToken(chatUserVO.getToken());
        String r = JsonUtil.pojoToJson(back);

        // 4. 远程调用
        BackVo backVo = new BackVo();
        backVo.setBack(r);
        log.info("backVo:{}", backVo);
        BaseResponse response = chatOpenFeignService.login(backVo);
        return response;
    }

    @Override
    public BaseResponse login2(ChatUserVo chatUserVO) {
        log.info("step1: 开始登录WEB GATE");
        // 1. 查询userId是否在redis中
        log.info("userId:{}", chatUserVO.getUserId());
        Long num = redisTemplate.opsForValue().size(RedisConstants.CHAT_USER_LOGIN_KEY + chatUserVO.getUserId());
        // 2. 如果不在则在mysql中查询
        DyUser dyUser = null;
        if(num==0){
            dyUser = chatMapper.selectByUserId(chatUserVO.getUserId());
            redisTemplate.opsForValue().set(RedisConstants.CHAT_USER_LOGIN_KEY + chatUserVO.getUserId(), dyUser);
        }else{
            dyUser = (DyUser) redisTemplate.opsForValue().get(RedisConstants.CHAT_USER_LOGIN_KEY + chatUserVO.getUserId());
            if(dyUser == null){
                log.error("用户不存在");
                return BaseResponse.fail("用户不存在");
            }
        }
        LoginBack back = new LoginBack();

        // 3. 取得所有节点
        List<ImNode> allWorker = imLoadBalance.getWorkers();
        back.setImNodeList(allWorker);
        ChatUserDTO userDTO = new ChatUserDTO();
        BeanUtils.copyProperties(chatUserVO, userDTO);
        userDTO.setToken(chatUserVO.getToken());
        back.setUserDTO(userDTO);
        back.setToken(chatUserVO.getToken());
        String r = JsonUtil.pojoToJson(back);

        // 4. 远程调用
        BackVo backVo = new BackVo();
        backVo.setBack(r);
        log.info("backVo:{}", backVo);
        BaseResponse response = chatOpenFeignService2.login(backVo);
        return response;
    }

    @Override
    public void sendChat(SendChat sendChat) {
        chatOpenFeignService.sendChat(sendChat);
    }
}
