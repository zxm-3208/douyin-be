package com.example.douyin_user.service.impl;

import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.domain.UserListDTO;
import com.example.douyin_user.domain.po.dbAuth.DyFollow;
import com.example.douyin_user.domain.vo.GetListVo;
import com.example.douyin_user.mapper.master.DyFollowMapper;
import com.example.douyin_user.service.FansListService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author : zxm
 * @date: 2024/5/15 - 19:05
 * @Description: com.example.douyin_user.service.impl
 * @version: 1.0
 */
@Service
@Slf4j
public class FansListServiceImpl implements FansListService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DyFollowMapper dyFollowMapper;

    @Autowired
    private MinioClient minioClient;
    // 普通文件桶
    @Value("${minio.bucket.icon}")
    private String bucket_icon_files;

    @Override
    public BaseResponse getFansList(GetListVo getListVo) {
        String userId = getListVo.getUserId();      // 目标用户
        String key = RedisConstants.USER_FANS_INFO_LIST_KEY + userId;
        List<String> userIdList = new ArrayList<>();
        List<String> iconList = new ArrayList<>();
        List<String> nameList = new ArrayList<>();
        List<String> introductionList = new ArrayList<>();

        // 1. 从Redis获取
        Long size = redisTemplate.opsForZSet().size(key);

        List<DyFollow> dyFansList = null;
        // 2. 判断是否存在
        if(size.equals(0L)){
            // 3. 如果不存在就从数据库中读取
            dyFansList = dyFollowMapper.getFansInfoByUserAndFollow(userId);
            List<String> followByUserList = dyFollowMapper.getFollowIdByUserId(userId);
            if(dyFansList.size()>0){
                for(DyFollow x: dyFansList) {
                    UserListDTO userListDTO = null;
                    log.info("userId:{}, followByUserList:{}", x.getUserId(), followByUserList);
                    if (followByUserList.contains(x.getUserId())) {
                        userListDTO = new UserListDTO(x.getUserId(), x.getDyUser().getIcon(), x.getDyUser().getUserName(), x.getDyUser().getIntroduction());
                    }else{
                        userListDTO = new UserListDTO(x.getUserId(), x.getDyUser().getIcon(), x.getDyUser().getUserName(), x.getDyUser().getIntroduction());
                    }
                    redisTemplate.opsForZSet().add(key, userListDTO, x.getFollowCreateTime().getTime());
                }
            }
        }
        // 4. 如果存在
        Set<ZSetOperations.TypedTuple<UserListDTO>> dto = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, 0, Long.valueOf(getListVo.getLastId()), Long.valueOf(getListVo.getOffset()), 100);

        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<UserListDTO> tuple : dto) {
            try {
                log.info("tuple:{}", tuple.getValue());
                userIdList.add(tuple.getValue().getUserId());
                if(tuple.getValue().getIcon()!=null && tuple.getValue().getIcon()!="")
                    iconList.add(minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_icon_files).object(tuple.getValue().getIcon()).method(Method.GET).build()));
                else
                    iconList.add(null);
                nameList.add(tuple.getValue().getUserName());
                introductionList.add(tuple.getValue().getIntroduction());
            }catch (Exception e){
                e.printStackTrace();
                return BaseResponse.fail("获取外链失败");
            }
            long time = tuple.getScore().longValue();
            if (time == minTime) {
                os++;
            }else {
                minTime = time;
                os = 1;
            }
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("userIdList", userIdList);
        map.put("iconList", iconList);
        map.put("nameList", nameList);
        map.put("introductionList", introductionList);
        // 返回结果
        return BaseResponse.success(map);
    }


}
