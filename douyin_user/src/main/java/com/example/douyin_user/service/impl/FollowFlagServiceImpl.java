package com.example.douyin_user.service.impl;

import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.domain.UserListDTO;
import com.example.douyin_user.domain.po.dbAuth.DyFollow;
import com.example.douyin_user.domain.vo.GetListVo;
import com.example.douyin_user.mapper.master.DyFollowMapper;
import com.example.douyin_user.service.FollowFlagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author : zxm
 * @date: 2024/5/17 - 22:08
 * @Description: com.example.douyin_user.service.impl
 * @version: 1.0
 */
@Service
@Slf4j
public class FollowFlagServiceImpl implements FollowFlagService {

    @Autowired
    private DyFollowMapper dyFollowMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public BaseResponse getFollowFlag(GetListVo getListVo) {
        String userId = getListVo.getUserId();          // 目标用户
        String realUserId = getListVo.getRealUserId();      // 真实用户
        String userIdFansListKey = RedisConstants.USER_FANS_INFO_LIST_KEY + userId;
        String userIdFollowsListKey = RedisConstants.USER_FOLLOW_INFO_LIST_KEY + userId;
        String realUserIdFollowsListKey = RedisConstants.USER_FOLLOW_INFO_LIST_KEY + realUserId;
        List<String> isFollowFlag = new ArrayList<>();
        if(getListVo.getIsFollow().equals("1")) {
            // 目标用户
            Set<ZSetOperations.TypedTuple<UserListDTO>> userList = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(userIdFollowsListKey, 0, Long.valueOf(getListVo.getLastId()), Long.valueOf(getListVo.getOffset()), 100);
            // 真实用户
            Set<ZSetOperations.TypedTuple<UserListDTO>> realUserList = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(realUserIdFollowsListKey, 0, Long.valueOf(getListVo.getLastId()), Long.valueOf(getListVo.getOffset()), 100);
            // 如果为空则从mysql中获取
            if(realUserList.size()==0){
                List<DyFollow> dyFollowList = dyFollowMapper.getFollowInfoByUserAndFollow(getListVo.getRealUserId());
                if(dyFollowList.size()>0){
                    for(DyFollow x: dyFollowList) {
                        UserListDTO userListDTO = new UserListDTO(x.getFollowerId(), x.getDyUser().getIcon(), x.getDyUser().getUserName(), x.getDyUser().getIntroduction());
                        redisTemplate.opsForZSet().add(realUserIdFollowsListKey, userListDTO, x.getFollowCreateTime().getTime());
                    }
                }
            }
            realUserList = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(realUserIdFollowsListKey, 0, Long.valueOf(getListVo.getLastId()), Long.valueOf(getListVo.getOffset()), 100);
            for(ZSetOperations.TypedTuple<UserListDTO> userTuple: userList){
                boolean flag = true;
                for(ZSetOperations.TypedTuple<UserListDTO> realUserTuple: realUserList){
                    if(userTuple.getValue().getUserId().equals(realUserId)){
                        isFollowFlag.add("-1");
                        flag = false;
                        break;
                    }
                    if(userTuple.getValue().equals(realUserTuple.getValue())){
                        isFollowFlag.add("1");
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    isFollowFlag.add("0");
                }
            }
        }else{
            // 目标用户
            Set<ZSetOperations.TypedTuple<UserListDTO>> userList = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(userIdFansListKey, 0, Long.valueOf(getListVo.getLastId()), Long.valueOf(getListVo.getOffset()), 100);
            // 真实用户
            Set<ZSetOperations.TypedTuple<UserListDTO>> realUserList = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(realUserIdFollowsListKey, 0, Long.valueOf(getListVo.getLastId()), Long.valueOf(getListVo.getOffset()), 100);
            // 如果为空则从mysql中获取
            if(realUserList.size()==0){
                List<DyFollow> dyFollowList = dyFollowMapper.getFollowInfoByUserAndFollow(getListVo.getRealUserId());
                log.info("mapperSize:{}", dyFollowList.size());
                if(dyFollowList.size()>0){
                    for(DyFollow x: dyFollowList) {
                        UserListDTO userListDTO = new UserListDTO(x.getFollowerId(), x.getDyUser().getIcon(), x.getDyUser().getUserName(), x.getDyUser().getIntroduction());
                        redisTemplate.opsForZSet().add(realUserIdFollowsListKey, userListDTO, x.getFollowCreateTime().getTime());
                    }
                }
            }
            realUserList = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(realUserIdFollowsListKey, 0, Long.valueOf(getListVo.getLastId()), Long.valueOf(getListVo.getOffset()), 100);

            for(ZSetOperations.TypedTuple<UserListDTO> userTuple: userList){
                boolean flag = true;
                for(ZSetOperations.TypedTuple<UserListDTO> realUserTuple: realUserList){
                    if(userTuple.getValue().getUserId().equals(realUserId)){
                        isFollowFlag.add("-1");
                        flag = false;
                        break;
                    }
                    if(userTuple.getValue().equals(realUserTuple.getValue())){
                        isFollowFlag.add("1");
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    isFollowFlag.add("0");
                }
            }
        }
        return BaseResponse.success(isFollowFlag);
    }
}
