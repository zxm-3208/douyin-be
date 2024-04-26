package com.example.douyin_publish.service.impl;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.example.douyin_commons.constant.Constants;
import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.constant.SystemConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.exception.MsgException;
import com.example.douyin_publish.domain.dto.CoverPublistDTO;
import com.example.douyin_publish.domain.dto.MediaPublistDTO;
import com.example.douyin_publish.domain.dto.UploadFileParamsDTO;
import com.example.douyin_publish.domain.dto.UploadFileResultDTO;
import com.example.douyin_publish.domain.po.DyMedia;
import com.example.douyin_publish.domain.po.DyPublish;
import com.example.douyin_publish.domain.vo.EditVo;
import com.example.douyin_publish.mapper.MediaFilesMapper;
import com.example.douyin_publish.mapper.PublishMapper;
import com.example.douyin_publish.service.UploadService;
import com.example.douyin_publish.utils.ExecutorsPools;
import com.example.douyin_publish.utils.MediaUtils;
import com.example.douyin_publish.utils.ZSetUtils;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.micrometer.common.util.StringUtils;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author : zxm
 * @date: 2024/4/9 - 12:26
 * @Description: com.example.douyin_publish.service.impl
 * @version: 1.0
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private PublishMapper publishMapper;

    @Autowired
    private SnowflakeGenerator snowflakeGenerator;

    @Autowired
    private ZSetUtils zSetUtils;

    private CountDownLatch countDownLatch;

//    @Autowired
//    UploadService currentProxy;

    // 普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_files;

    // 普通文件桶
    @Value("${minio.bucket.videofiles}")
    private String bucket_videofiles;

    private DyMedia dyMedia;
    private DyPublish dyPublish;

    public static File[] files = null;

    @Override
    public UploadFileResultDTO uploadFile(UploadFileParamsDTO uploadFileParamsDTO, byte[] bytes, String folder, String objectName) {
        // 生成文件id, 雪花算法
        String fileId = String.valueOf(snowflakeGenerator.next());
        uploadFileParamsDTO.getDyMedia().setId(fileId);
        uploadFileParamsDTO.getDyPublish().setMediaId(fileId);

        // 文件名称
        String filename = uploadFileParamsDTO.getDyPublish().getFileName();
        // 构造objectname
        if(StringUtils.isEmpty(objectName)){
            objectName = fileId + filename.substring(filename.lastIndexOf("."));
        }
        // 通过日期构造文件存储路径
        if(StringUtils.isEmpty(folder)){
            folder = getFileFolder(new Date(), true, true, true);
        } else if (folder.indexOf("/")<0) {
            folder = folder + "/";
        }
        // 对象名称
        objectName = folder + objectName;

        try{
            // 上传至文件系统
            addMediaFilesToMinIO(bytes, objectName, bucket_files, uploadFileParamsDTO.getContentType());
            // 写入数据库表
            getService().addMediaFilesToDb(uploadFileParamsDTO, objectName);
            // 写入Redis
            addMediaFilesToSingleRedis(uploadFileParamsDTO);
            UploadFileResultDTO uploadFileResultDTO = UploadFileResultDTO.success();
            return uploadFileResultDTO; // 返回结果
        }catch (Exception e){
            e.printStackTrace();
            MsgException.cast("上传过程中出错");
        }
        return UploadFileResultDTO.failIndex(uploadFileParamsDTO.getChunk());
    }

    @Override
    public UploadFileResultDTO uploadCoverFile(UploadFileParamsDTO uploadFileParamsDTO, byte[] bytes) {
        // 生成文件id, 雪花算法
        String fileId = uploadFileParamsDTO.getDyPublish().getMediaId();

        // 文件名称
        String filename = uploadFileParamsDTO.getDyPublish().getFileName();
        // 构造objectname
        String objectName = fileId + filename.substring(filename.lastIndexOf("."));

        // 通过日期构造文件存储路径
        String folder = getFileFolder(new Date(), true, true, true);
        // 对象名称
        objectName = folder + objectName;

        try{
            // 上传至文件系统
            addMediaFilesToMinIO(bytes, objectName, bucket_files, uploadFileParamsDTO.getContentType());
            // 写入数据库表
            getService().addCoverFilesToDb(fileId, objectName);
            // 写入Redis
            addMediaFilesToSingleRedis(uploadFileParamsDTO);
            UploadFileResultDTO uploadFileResultDTO = UploadFileResultDTO.success();
            return uploadFileResultDTO; // 返回结果
        }catch (Exception e){
            e.printStackTrace();
            MsgException.cast("上传过程中出错");
        }
        return UploadFileResultDTO.failIndex(uploadFileParamsDTO.getChunk());
    }


    public void uploadCoverFile(UploadFileParamsDTO uploadFileParamsDTO, String filePath) {
        // 获取文件id
        String fileId = uploadFileParamsDTO.getDyPublish().getMediaId();

        // 构造objectname
        String objectName = fileId + Constants.COVEREXTNAME;

        // 通过日期构造文件存储路径
        String folder = getFileFolder(new Date(), true, true, true);
        // 对象名称
        objectName = folder + objectName;

        uploadFileParamsDTO.getDyPublish().setImgUrl(objectName);

        try{
            // 上传至文件系统
            addMediaFilesToMinIO(filePath, bucket_files, objectName);
//            // 写入数据库表
//            getService().addCoverFilesToDb(fileId, objectName);
//            // 写入Redis
//            addMediaFilesToSingleRedis(uploadFileParamsDTO);
            UploadFileResultDTO uploadFileResultDTO = UploadFileResultDTO.success();
//            return uploadFileResultDTO;
        }catch (Exception e){
            e.printStackTrace();
            MsgException.cast("上传过程中出错");
        }
//        return UploadFileResultDTO.failIndex(uploadFileParamsDTO.getChunk());
    }

    @Override
    public UploadFileResultDTO uploadChunk(UploadFileParamsDTO uploadFileParamsDTO, byte[] bytes) {
        // 得到分块文件的目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(uploadFileParamsDTO.getDyMedia().getMd5());
        // 得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + uploadFileParamsDTO.getChunk();
        try{
            // 将文件存储至minIO
            addMediaFilesToMinIO(bytes, chunkFilePath, bucket_videofiles, uploadFileParamsDTO.getContentType());
            log.info("上传至minIO:分片索引{}/总片数{}",uploadFileParamsDTO.getChunk(),uploadFileParamsDTO.getChunks());
            // 把成功传输的分片索引写入Redis
            addMediaFilesToChunkRedis(uploadFileParamsDTO);
            return UploadFileResultDTO.successIndex(uploadFileParamsDTO.getChunk());
        } catch (Exception e) {
            return UploadFileResultDTO.failIndex(uploadFileParamsDTO.getChunk());
        }
    }

    @Override
    public UploadFileResultDTO mergeChunk(UploadFileParamsDTO uploadFileParamsDTO) {
        String fileMd5 = uploadFileParamsDTO.getDyMedia().getMd5();
        String fileName = uploadFileParamsDTO.getDyPublish().getFileName();
        // 扩展名
        String extName = fileName.substring(fileName.lastIndexOf("."));

        // 将临时文件上传至minio
        String mergeFilePath = getFilePathByMd5(fileMd5, extName);

        File[] chunkFIles = null;
        // 创建临时文件作为合并文件
        File mergeFile = null;

        // 生成文件id, 雪花算法
        String fileId = String.valueOf(snowflakeGenerator.next());
        uploadFileParamsDTO.getDyMedia().setId(fileId);
        uploadFileParamsDTO.getDyPublish().setMediaId(fileId);


        try {
            if(!checkFileIsExist(bucket_videofiles,mergeFilePath)) {
                // 下载所有分块文件
                chunkFIles = checkChunkStatus(fileMd5, uploadFileParamsDTO.getChunks());
                try {
                    mergeFile = File.createTempFile(fileMd5, extName);
                } catch (IOException e) {
                    MsgException.cast("合并文件过程中创建临时文件错误");
                }

                try (RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw")) {
                    // 开始合并
                    byte[] b = new byte[1024];
                    for (File file : chunkFIles) {
                        try (RandomAccessFile raf_read = new RandomAccessFile(file, "r")) {
                            int len = -1;
                            while ((len = raf_read.read(b)) != -1) {
                                // 向合并文件写数据
                                raf_write.write(b, 0, len);
                            }
                        }
                    }
                } catch (IOException e) {
                    MsgException.cast("合并文件过程出错");
                }
                log.info("合并文件完成{}", mergeFile.getAbsolutePath());
                uploadFileParamsDTO.setFileSize(mergeFile.length());

                try (InputStream mergeFileInputStream = new FileInputStream(mergeFile)) {
                    // 对文件进行校验，通过比较md5值
                    String newFileMd5 = DigestUtils.md5Hex(mergeFileInputStream);
                    if (!fileMd5.equalsIgnoreCase(newFileMd5)) {
                        // 校验失败
                        MsgException.cast("合并文件校验失败");
                    }
                    log.info("合并文件校验通过{}", mergeFile.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    // 校验失败
                    MsgException.cast("合并文件校验异常");
                }

                try {
                    // 获取视频封面
                    Map<String, String> screenShot = MediaUtils.getScreenshot(mergeFile.getAbsolutePath());
                    // 上传封面信息
                    uploadCoverFile(uploadFileParamsDTO, screenShot.get("imgPath"));
                    // 上传文件到minIO  TODO: 这一步后续可以用消息队列实现（前端可以选择继续等待实现视频预览或者跳过预览直接跳转到信息编辑页面）
                    addMediaFilesToMinIO(mergeFile.getAbsolutePath(), bucket_videofiles, mergeFilePath);
                    log.info("合并文件上传minIO完成{}", mergeFile.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    MsgException.cast("合并文件时上传文件错误");
                }
            }

            // 写入数据库
//            System.out.println(getService().getClass());
            Boolean is_finish = getService().addMediaFilesToDb(uploadFileParamsDTO, mergeFilePath);
            log.info("写入数据库：{}", is_finish);
            // 写入Redis
            addMediaFilesToMergeRedis(uploadFileParamsDTO);
            log.info("写入了Redis");
            if (!is_finish) {
                MsgException.cast("媒资文件入库错误");
            }
            return UploadFileResultDTO.successMerge(fileId);
        }finally {
            // 删除临时分块文件
            if(chunkFIles != null){
                for(File chunkFile: chunkFIles){
                    if(chunkFile.exists()){
                        chunkFile.delete();
                    }
                }
            }
            // 删除合并的临时文件
            if(mergeFile!=null){
                mergeFile.delete();
            }
        }
    }

    /**
     * @description: 根据MD5获得合并文件的地址
     * @param fileMd5
     * @param extName
     * @return: java.lang.String
     * @author zxm
     * @date: 2024/4/15 14:11
     */
    private String getFilePathByMd5(String fileMd5, String extName) {
        return fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" +fileMd5 + "/" + fileMd5 +extName;
    }

    /**
     * @description: 得到分块文件的目录
     * @param fileMd5
     * @return: java.lang.String
     * @author zxm
     * @date: 2024/4/15 10:33
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
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
                        .stream(byteArrayInputStream, byteArrayInputStream.available(), -1) // -1表示文件分片按(不小于5M，不大于5T),分片数量最大10000
                        .contentType(contentType)
                        .build();
                minioClient.putObject(putObjectArgs);
            } catch (Exception e) {
                e.printStackTrace();
                MsgException.cast("上传文件到文件系统出错");
            }
        }
    }

    /**
     * @description: 将文件写入minIO(文件路径)
     * @param filePath
     * @param bucket
     * @param objectName
     * @return: void
     * @author zxm
     * @date: 2024/4/15 10:55
     */
    private void addMediaFilesToMinIO(String filePath, String bucket, String objectName){
        if(!checkFileIsExist(bucket, objectName)){
            try {
                UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .filename(filePath)
                        .build();
                minioClient.uploadObject(uploadObjectArgs);
            } catch (Exception e) {
                MsgException.cast("文件上传到文件系统失败");
            }
        }
    }

    /**
     * @description: 将文件信息添加到文件夹
     * @param fileMd5 文件md5值
     * @param uploadFileParamsDTO 上传文件的信息
     * @param objectName  对象名称
     * @return: com.example.douyin_publish.domain.po.DyMedia
     * @author zxm
     * @date: 2024/4/9 15:44
     */
    @Transactional
    public boolean addMediaFilesToDb(UploadFileParamsDTO uploadFileParamsDTO, String objectName) {
        String contentType = GetContentType(null, objectName);
        if(contentType.indexOf("image")<0) {
            // 从数据库查询文件
            dyMedia = mediaFilesMapper.selectById(uploadFileParamsDTO.getDyMedia().getId());
            dyPublish = publishMapper.selectByMediaId(uploadFileParamsDTO.getDyMedia().getId());
            if (dyMedia == null) {
                dyMedia = new DyMedia();
                // 拷贝基本信息
                BeanUtils.copyProperties(uploadFileParamsDTO.getDyMedia(), dyMedia);

                dyMedia.setMediaUrl(objectName);
                //            dyMedia.setMd5(uploadFileParamsDTO.);
                //保存文件信息到DyMedia表
                int insert = mediaFilesMapper.insert(dyMedia);
                if (insert < 0) {
                    MsgException.cast("保存文件信息失败");
                }
            }
            if (dyPublish == null) {
                dyPublish = new DyPublish();
                // 拷贝基本信息
                BeanUtils.copyProperties(uploadFileParamsDTO.getDyPublish(), dyPublish);
//                if (contentType.indexOf("image") >= 0) {
//                    dyPublish.setImgUrl("/" + bucket_videofiles + "/" + objectName);
//                }
                log.info("用户ID{}",uploadFileParamsDTO.getDyPublish().getAuthor());
                dyPublish.setAuthor(uploadFileParamsDTO.getDyPublish().getAuthor());
                if(dyPublish.getUploadTime()==null)
                    dyPublish.setUploadTime(new Timestamp(System.currentTimeMillis()));
                dyPublish.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                //保存文件信息到DyPublish表
                int insert = publishMapper.insert(dyPublish);
                if (insert < 0) {
                    MsgException.cast("保存文件信息失败");
                }
            }
        }else{
            dyPublish = publishMapper.selectByMediaId(uploadFileParamsDTO.getDyMedia().getId());
            // 拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDTO.getDyPublish(), dyPublish);
            dyPublish.setImgUrl("/" + bucket_files + "/" + objectName);
            if(dyPublish.getUploadTime()==null)
                dyPublish.setUploadTime(new Timestamp(System.currentTimeMillis()));
            dyPublish.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            //保存文件信息到DyPublish表
            int insert = publishMapper.insert(dyPublish);
            if (insert < 0) {
                MsgException.cast("保存文件信息失败");
            }
        }
        return true;
    }

    @Transactional
    public Boolean addCoverFilesToDb(String fileId, String objectName) {
        String contentType = GetContentType(null, objectName);
        dyPublish = publishMapper.selectByMediaId(fileId);
        // 拷贝基本信息
//        BeanUtils.copyProperties(uploadFileParamsDTO.getDyPublish(), dyPublish);
        dyPublish.setMediaId(fileId);
        dyPublish.setImgUrl(objectName);
        if(dyPublish.getUploadTime()==null)
            dyPublish.setUploadTime(new Timestamp(System.currentTimeMillis()));
        dyPublish.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        //保存文件信息到DyPublish表
        int update = publishMapper.updateImgUrl(dyPublish.getMediaId(), dyPublish.getImgUrl());
        if (update < 0) {
            MsgException.cast("保存文件信息失败");
        }
        return true;
    }

    @Override
    public UploadFileResultDTO editPublist(EditVo editVo) {
        String title = editVo.getTitle();
        // 读取数据库
        int update = publishMapper.updateTitle(editVo.getMediaId(), editVo.getTitle());
        Long scope = publishMapper.selectByMediaId(editVo.getMediaId()).getUpdateTime().getTime();

        if (update < 0) {
            MsgException.cast("保存文件信息失败");
        }
        // 将发布的视频推送给Redis
        CoverPublistDTO coverPublistDTO = new CoverPublistDTO(editVo.getMediaId(), editVo.getCoverUrl());
        MediaPublistDTO mediaPublistDTO = new MediaPublistDTO(editVo.getMediaId(), editVo.getMediaUrl());

        zSetUtils.addObjectToZSet(RedisConstants.PUBLIST_USER_COVER_KEY + editVo.getUserId(), coverPublistDTO, scope);
        redisTemplate.expire(RedisConstants.PUBLIST_USER_COVER_KEY + editVo.getUserId(), RedisConstants.PUBLIST_USER_COVER_TTL, TimeUnit.DAYS);

        zSetUtils.addObjectToZSet(RedisConstants.PUBLIST_USER_MEDIA_KEY + editVo.getUserId(), mediaPublistDTO, scope);
        redisTemplate.expire(RedisConstants.PUBLIST_USER_MEDIA_KEY + editVo.getUserId(), RedisConstants.PUBLIST_USER_MEDIA_TTL, TimeUnit.DAYS);

        return UploadFileResultDTO.success();
    }


    @Override
    public BaseResponse checkFile(String fileMd5) {
        // 1. 查询Redis中是否存在MD5
        String is_finish = String.valueOf(redisTemplate.opsForValue().get(RedisConstants.MEDIA_MERGEMD5_KEY + fileMd5));

        // 2. 如果md5码在redis中存在，且value为1，则返回1
        if(is_finish!=null && is_finish.equals("1")){
            return new BaseResponse(1, null);
        }

        // 3. 如果redis中不存在，则在mysql中查找，如果存在也返回1
        int count = mediaFilesMapper.getCountOfMediaByMD5(fileMd5);
        if(count>0){
            return new BaseResponse(1, null);
        }

        // 4. 如果md5码在redis中存在，但value为0，则返回2
        if(is_finish!=null && is_finish.equals("0")){
            Set chunkFile = redisTemplate.opsForSet().members(RedisConstants.MEDIA_CHUNKMD5_KEY + fileMd5);
            log.info("chunkFile{}",chunkFile);
            return new BaseResponse(2, chunkFile);
        }

        // 5. 如果mysql中不存在，则返回0
        if(count==0){
            return new BaseResponse(0, null);
        }
        return BaseResponse.fail("查询失败");
    }

    /**
     * @description: 检查所有分块是否上传完毕
     * @param fileMd5
     * @param chunkTotal
     * @return: java.io.File[]
     * @author zxm
     * @date: 2024/4/15 13:18
     */
    private File[] checkChunkStatus(String fileMd5, int chunkTotal){
        files = new File[chunkTotal];
        // 得到分块文件的目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 检查分块文件是否上传完毕
//        log.info("=={}",redisTemplate.opsForSet().size(RedisConstants.MEDIA_CHUNKMD5_KEY + fileMd5));
//        log.info("=={}",chunkTotal);
        if(!redisTemplate.opsForSet().size(RedisConstants.MEDIA_CHUNKMD5_KEY + fileMd5).equals((long)chunkTotal)) {
            MsgException.cast("分块文件缺失");
            return null;
        }
        // 下载
        //在线程池执行之前，给计数器指定数值（与要执行代码的次数一致）
        countDownLatch = new CountDownLatch(chunkTotal);
        for (int i = 0; i < chunkTotal; i++) {
            // 创建分片下载任务对象
            DownloadRunnable downloadRunnable = new DownloadRunnable(chunkFileFolderPath, i, bucket_videofiles, chunkTotal);
            // 使用线程池
            ExecutorsPools.fixedThreadPool.execute(downloadRunnable);
//                String chunkFilePath = chunkFileFolderPath + i;
//                // 下载文件
//                File chunkFile = null;
//                try {
//                    chunkFile = File.createTempFile("chunk" + i, null);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    MsgException.cast("下载分块时创建临时文件出错");
//                }
//                downloadFileFromMinIO(chunkFile, bucket_videofiles, chunkFilePath);
//                files[i] = chunkFile;
//                log.info("分片下载进度{}/{}", i, chunkTotal);
        }
        try {
            countDownLatch.await();
            //等待计数器归零
            log.info("多线程结束");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return files;
    }

    public void downloadFileFromMinIO(File file, String bucket, String objectName) {
        InputStream fileInputStream = null;
        OutputStream fileOutputStream = null;
        try{
            fileInputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build());
            try{
                fileOutputStream = new FileOutputStream(file);
                IOUtils.copy(fileInputStream, fileOutputStream);
            }catch (IOException e){
                MsgException.cast("下载文件"+objectName+"出错");
            }
        } catch (Exception e) {
            e.printStackTrace();
            MsgException.cast("文件不存在"+objectName);
        }finally {
            if(fileInputStream!=null){
                try{
                    fileInputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(fileOutputStream!=null){
                try{
                    fileOutputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @description: 将成功传输的分片文件索引保存信息记录到Redis中
     * @param uploadFileParamsDTO
     * @return: void
     * @author zxm
     * @date: 2024/4/14 21:14
     */
    private void addMediaFilesToChunkRedis(UploadFileParamsDTO uploadFileParamsDTO) {
        String key = RedisConstants.MEDIA_CHUNKMD5_KEY + uploadFileParamsDTO.getDyMedia().getMd5();
        redisTemplate.opsForSet().add(key, uploadFileParamsDTO.getChunk());
        redisTemplate.opsForValue().set(RedisConstants.MEDIA_MERGEMD5_KEY + uploadFileParamsDTO.getDyMedia().getMd5(), "0", RedisConstants.MEDIA_CHUNKMD5_TTL, TimeUnit.MINUTES);
//        if (redisTemplate.opsForSet().size(key).equals((long) uploadFileParamsDTO.getChunks())){
//            redisTemplate.opsForValue().set(RedisConstants.MEDIA_MERGEMD5_KEY + uploadFileParamsDTO.getDyMedia().getMd5(), "1");
//        }
//        else {
//            redisTemplate.opsForValue().set(RedisConstants.MEDIA_MERGEMD5_KEY + uploadFileParamsDTO.getDyMedia().getMd5(), "0");
//        }
    }

    /**
     * @description: 将合并成功的文件记录到Redis中
     * @param uploadFileParamsDTO
     * @return: void
     * @author zxm
     * @date: 2024/4/15 16:11
     */
    private void addMediaFilesToMergeRedis(UploadFileParamsDTO uploadFileParamsDTO) {
        redisTemplate.opsForValue().set(RedisConstants.MEDIA_MERGEMD5_KEY + uploadFileParamsDTO.getDyMedia().getMd5(), "1", RedisConstants.MEDIA_MERGEMD5_TTL, TimeUnit.MINUTES);
        if(redisTemplate.opsForSet().size(RedisConstants.MEDIA_CHUNKMD5_KEY + uploadFileParamsDTO.getDyMedia().getMd5())!=0){
            redisTemplate.delete(RedisConstants.MEDIA_CHUNKMD5_KEY + uploadFileParamsDTO.getDyMedia().getMd5());
        }
    }

    /**
     * @description: 单个文件传输信息记录到Redis中
     * @param uploadFileParamsDTO
     * @return: void
     * @author zxm
     * @date: 2024/4/14 21:14
     */
    private void addMediaFilesToSingleRedis(UploadFileParamsDTO uploadFileParamsDTO){
        if(uploadFileParamsDTO.getContentType().indexOf("image")>=0) {
            redisTemplate.opsForValue().set(RedisConstants.COVERMD5_KEY + uploadFileParamsDTO.getDyMedia().getMd5(), "1", RedisConstants.COVERMD5_TTL, TimeUnit.MINUTES);
        }
        else {
            redisTemplate.opsForValue().set(RedisConstants.MEDIA_MERGEMD5_KEY + uploadFileParamsDTO.getDyMedia().getMd5(), "1", RedisConstants.MEDIA_MERGEMD5_TTL, TimeUnit.MINUTES);
            if(redisTemplate.opsForSet().size(RedisConstants.MEDIA_CHUNKMD5_KEY + uploadFileParamsDTO.getDyMedia().getMd5())!=0){
                redisTemplate.delete(RedisConstants.MEDIA_CHUNKMD5_KEY + uploadFileParamsDTO.getDyMedia().getMd5());
            }
        }
    }

    @Override
    public BaseResponse downloadCreative(String fileMd5) {
        if (fileMd5 == null) {
            return BaseResponse.fail("文件id为空！");
        }
        String objectName = getFilePathByMd5(fileMd5, Constants.DOWNLOADEXTNAME);
        Boolean bucketExists = null;
        try {
            bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket_videofiles).build());
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.fail("判断桶是否存在出现异常");
        }
        if (!bucketExists) {
            log.error("桶不存在");
            return BaseResponse.fail("桶不存在");
        }
        Boolean objectExist = checkFileIsExist(bucket_videofiles, objectName);
        if(!objectExist){
            log.error("文件不存在");
            return BaseResponse.fail("文件不存在");
        }
        // 获取外链，链接失效时间7天
        String url = null;
        try {
            url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_videofiles).object(objectName).method(Method.GET).build());
        }catch (Exception e){
            e.printStackTrace();
            return BaseResponse.fail("获取外链失败");
        }
        // 获取mediaId
        String mediaId = String.valueOf(mediaFilesMapper.getMediaIdByMd5(fileMd5));
        HashMap<String, Object> map = new HashMap<>();
        map.put("mediaId", mediaId);
        map.put("url", url);
        log.info("获得得外链为:{}",map.get("url"));
        return BaseResponse.success(map);
    }

    @Override
    public BaseResponse downloadCover(String mediaId) {
        DyPublish dyPublish = publishMapper.selectByMediaId(mediaId);

        String objectName = dyPublish.getImgUrl();

        if (objectName == null) {
            return BaseResponse.fail("封面不存在！");
        }
        Boolean bucketExists = null;
        try {
            bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket_files).build());
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.fail("判断桶是否存在出现异常");
        }
        if (!bucketExists) {
            log.error("桶不存在");
            return BaseResponse.fail("桶不存在");
        }
        log.info("bucket_files:{}",bucket_files);
        log.info("objectName:{}",objectName);
        Boolean objectExist = checkFileIsExist(bucket_files, objectName);
        if(!objectExist){
            log.error("文件不存在");
            return BaseResponse.fail("文件不存在");
        }
        // 获取外链，链接失效时间7天
        String url = null;
        try {
            url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_files).object(objectName).method(Method.GET).build());
        }catch (Exception e){
            e.printStackTrace();
            return BaseResponse.fail("获取外链失败");
        }
        log.info("封面的外链为：{}",url);
        return BaseResponse.success(url);
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
     *  通过AopContext获取代理类
     */
    private UploadService getService(){
        return Objects.nonNull(AopContext.currentProxy()) ? (UploadService) AopContext.currentProxy() : this;
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

    public void countDown(){
        countDownLatch.countDown();
    }

}
