package com.example.douyin_publish.mapper.master;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_publish.domain.po.DyMedia;
import com.example.douyin_publish.domain.po.DyPublish;
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
    DyPublish selectByMediaId(String id);

    @Update("update dy_publish u set img_url=#{imgUrl} where mediaId=#{mediaId}")
    int updateImgUrl(String mediaId, String imgUrl);

    @Update("update dy_publish u set title=#{title} where mediaId=#{mediaId}")
    int updateTitle(String mediaId, String title);

    @Update("update dy_publish u set status=#{status} where mediaId=#{mediaId}")
    int updateStatus(String mediaId, String status);

    @Select("select * from dy_publish u where u.author = #{userId} and u.status='1' ")
    DyPublish[] selectByUserId(String userId);

    @Insert("insert into dy_publish(id, mediaId, fileName, title, uploadTime, updateTime, author, status, type, imgUrl, tag) values(#{id}, #{mediaId}, #{fileName}, #{title}, #{uploadTime}, #{updateTime}, #{author}, #{status}, #{type}, #{imgUrl}, #{tag})")
    int insert(DyPublish dyPublish);

    List<DyPublish> findUrl();
}
