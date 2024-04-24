package com.example.douyin_publish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_publish.domain.po.DyPublish;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author : zxm
 * @date: 2024/4/9 - 15:02
 * @Description: com.example.douyin_publish.mapper
 * @version: 1.0
 */
public interface PublishMapper extends BaseMapper<DyPublish> {

    @Select("select * from dy_publish u where u.media_id = #{id}")
    DyPublish selectByMediaId(String id);

    @Update("update dy_publish u set img_url=#{imgUrl} where media_id=#{mediaId}")
    int updateImgUrl(String mediaId, String imgUrl);

    @Update("update dy_publish u set title=#{title} where media_id=#{mediaId}")
    int updateTitle(String mediaId, String title);

    @Select("select img_url from dy_publish u where u.author = #{userId}")
    String[] selectByUserId(String userId);

}
