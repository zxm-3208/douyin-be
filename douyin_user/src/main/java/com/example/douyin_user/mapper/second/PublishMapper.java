package com.example.douyin_user.mapper.second;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_user.domain.po.dbMedia.DyPublish;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author : zxm
 * @date: 2024/4/9 - 15:02
 * @Description: com.example.douyin_publish.mapper
 * @version: 1.0
 */
public interface PublishMapper extends BaseMapper<DyPublish> {

    @Select("select * from dy_publish u where u.mediaId = #{id}")
    DyPublish getCoverUrlByMediaId(String mediaId);
}
