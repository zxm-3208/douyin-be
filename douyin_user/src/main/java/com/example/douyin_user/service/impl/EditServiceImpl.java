package com.example.douyin_user.service.impl;

import com.example.douyin_commons.constant.Constants;
import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.domain.EditDTO;
import com.example.douyin_commons.core.domain.ResultCode;
import com.example.douyin_commons.core.exception.MsgException;
import com.example.douyin_user.domain.po.dbAuth.DyUser;
import com.example.douyin_user.domain.vo.SubmitEditVO;
import com.example.douyin_user.mapper.master.DyUserMapper;
import com.example.douyin_user.service.EditService;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private MinioClient minioClient;

    // 普通文件桶
    @Value("${minio.bucket.icon}")
    private String bucket_icon_files;

    @Override
    public BaseResponse getAttribute(String userId) {
        String key = RedisConstants.USER_EDIT_KEY + userId;
        Long size = redisTemplate.opsForHash().size(key);
        Map map = new HashMap<String, String>();
        if(size.equals(0L)){
            DyUser edit = dyUserMapper.getEdit(userId);
            if(edit!=null){
                log.info("查询用户信息成功:{}",edit);
                String iconUrl = null;
                try {
                    if(edit.getIcon()!=null)
                        iconUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_icon_files).object(edit.getIcon()).method(Method.GET).build());
                } catch (Exception e) {
                    e.printStackTrace();
                    return BaseResponse.fail("获取外链失败");
                }
                map.put("icon", iconUrl);
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

    @Override
    public BaseResponse submitEdit(SubmitEditVO submitEditVO) {
        String userId = submitEditVO.getUserId();
        MultipartFile file = submitEditVO.getFile();
        String editName = submitEditVO.getEditName();
        String editIntro = submitEditVO.getEditIntro();
        String gender = submitEditVO.getGender();
        String birthday = submitEditVO.getBirthday();
        String defaultIcon = submitEditVO.getDefaultIcon();
        byte[] bytes = null;
        String objectName = null;
        // 1. 存储icon
        if ("0".equals(defaultIcon)){
            try {
                bytes = file.getBytes();
            } catch (IOException e) {
                MsgException.cast("上传文件过程中出错");
                return BaseResponse.fail("上传文件过程中出错");
            }
            // 获取文件id
            String fileId = file.getOriginalFilename();
            // 构造objectname
            objectName = fileId;
            // 通过日期构造文件存储路径
            String folder = getFileFolder(new Date(), true, true, true);
            // 对象名称
            objectName = folder + objectName;
            addMediaFilesToMinIO(bytes, objectName, bucket_icon_files, file.getContentType());
        }


        // 3. 存入Mysql
        Integer i = dyUserMapper.updateByUserId(userId, objectName, editName, editIntro, gender, new Date(Long.parseLong(birthday)));
        if(i==0){
            log.info("mysql数据更新失败！");
            return BaseResponse.fail("数据插入mysql失败！");
        }
        log.info("mysql数据更新:{}条", i);
        String key = RedisConstants.USER_EDIT_KEY + userId;
        redisTemplate.opsForHash().delete(key, "birthday", "userId", "icon", "gender", "editIntro", "editName");
        log.info("redis缓存删除:{}", key);
        return BaseResponse.success();
    }

    private String getFileFolder(Date date, boolean year, boolean month, boolean day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 获取当前日期字符串
        String dateString = sdf.format(date);
        // 取出年、月、日
        String[] dateStringArray = dateString.split("-");
        StringBuffer folderString = new StringBuffer();
        if(year){
            folderString.append(dateStringArray[0]);
            folderString.append("/");
        }
        if(month){
            folderString.append(dateStringArray[1]);
            folderString.append("/");
        }
        if(year){
            folderString.append(dateStringArray[2]);
            folderString.append("/");
        }
        return folderString.toString();
    }

    /**
     * 判断文件是否存在
     */
    public boolean checkFileIsExist(String bucket, String objectName){
        try {
            minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(objectName).build()
            );
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * @description: 将文件写入minIO(流传输)
     * @param bytes 文件字节数组
     * @param bucket 桶
     * @param objectName 对象名称
     * @param contentType 内容类型 （即是Internet Media Type，互联网媒体类型，也叫做MIME类型，通过这个属性来告诉服务器如何处理请求的数据。）
     * @return: void
     * @author zxm
     * @date: 2024/4/9 15:25
     */
    private void addMediaFilesToMinIO(byte[] bytes, String objectName, String bucket, String contentType) {
        if(!checkFileIsExist(bucket, objectName)) {
            contentType = GetContentType(contentType, objectName);
            // 转为流
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            try {
                PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucket)
                        .object(objectName)
                        .contentType(contentType)
                        .stream(byteArrayInputStream, bytes.length, 10 * 1024 * 1024)
                        .build();
                minioClient.putObject(putObjectArgs);
            } catch (Exception e) {
                e.printStackTrace();
                MsgException.cast("上传文件到文件系统出错");
            }
        }
    }

    private String GetContentType(String contentType, String objectName){
        if(contentType == null){
            //资源的媒体类型
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//默认未知二进制流
            if(objectName.indexOf(".")>=0){
                // 取objectName中的扩展名
                String extension = objectName.substring(objectName.lastIndexOf("."));
                ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
                if(extensionMatch != null){
                    contentType = extensionMatch.getMimeType();
                }
            }
        }
        return contentType;
    }

}
