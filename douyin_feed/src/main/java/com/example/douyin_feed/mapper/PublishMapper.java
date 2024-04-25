package com.example.douyin_feed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_feed.domain.po.DyPublish;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author : zxm
 * @date: 2024/4/9 - 15:02
 * @Description: com.example.douyin_publish.mapper
 * @version: 1.0
 */
public interface PublishMapper extends BaseMapper<DyPublish> {

    @Select("select img_url from dy_publish u where u.media_id = #{id}")
    String getCoverUrlByid(String id);

}
