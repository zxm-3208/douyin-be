package com.example.douyin_publish.controller;

import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.exception.MsgException;
import com.example.douyin_publish.domain.dto.UploadFileParamsDTO;
import com.example.douyin_publish.domain.dto.UploadFileResultDTO;
import com.example.douyin_publish.domain.po.DyMedia;
import com.example.douyin_publish.domain.po.DyPublish;
import com.example.douyin_publish.domain.vo.*;
import com.example.douyin_publish.mapper.PublishMapper;
import com.example.douyin_publish.service.UploadService;
import com.sun.tools.jconsole.JConsoleContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author : zxm
 * @date: 2024/4/10 - 19:27
 * @Description: com.example.douyin_publish.controller
 * @version: 1.0
 */

@RestController     //@Controller + @ResponseBody
@RequestMapping("/publish")
@CrossOrigin
@Slf4j
public class PublishController {

    @Autowired
    UploadService uploadService;


    /**
     * @description: 普通文件上传
     * @param uploadVo
     * @param folder
     * @param objectName
     * @return: com.example.douyin_publish.domain.dto.UploadFileResultDTO
     * @author zxm
     * @date: 2024/4/15 11:08
     */
    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})   // comsume用来控制入参的数据类型
    // RequestParam一般用于name-valueString类型的请求域，RequestPart用于复杂的请求域.使用@RequestBody接收对象，所对应的content-type:application/json
    public UploadFileResultDTO upload(UploadVo uploadVo,
                                      @RequestParam(value = "folder", required = false) String folder,
                                      @RequestParam(value = "objectName", required = false) String objectName){
        log.info("{}",uploadVo);
        // 将Vo的数据传给DTO
        UploadFileParamsDTO uploadFileParamsDto = new UploadFileParamsDTO(new DyMedia(),new DyPublish());
        String contentType = uploadVo.getFile().getContentType();
        uploadFileParamsDto.setContentType(contentType);
        uploadFileParamsDto.setFileSize(uploadVo.getFile().getSize());
        uploadFileParamsDto.getDyMedia().setMd5(uploadVo.getMd5());
        uploadFileParamsDto.getDyPublish().setAbout(uploadVo.getUid());

        if(contentType.indexOf("image")>=0){
            uploadFileParamsDto.getDyPublish().setType("001001");   //是个图片
        }
        else{
            uploadFileParamsDto.getDyPublish().setType("001003");   //是个视频
        }
        uploadFileParamsDto.getDyPublish().setFileName(uploadVo.getName());
        UploadFileResultDTO uploadFileResultDTO = null;
        try{
            uploadFileResultDTO = uploadService.uploadFile(uploadFileParamsDto, uploadVo.getFile().getBytes(), null, null);
        } catch (IOException e) {
            MsgException.cast("上传文件过程中出错");
        }
        return uploadFileResultDTO;
    }

    @PostMapping("/mergechunks")
    public UploadFileResultDTO mergechunks(@RequestBody MergeChunksVo mergeChunksVO){
        UploadFileParamsDTO uploadFileParamsDTO = new UploadFileParamsDTO(new DyMedia(),new DyPublish());
        uploadFileParamsDTO.getDyPublish().setFileName(mergeChunksVO.getFileName());
        uploadFileParamsDTO.getDyMedia().setMd5(mergeChunksVO.getFileMd5());
        uploadFileParamsDTO.setChunks(mergeChunksVO.getChunkTotal());
        uploadFileParamsDTO.getDyPublish().setType("001002");
        return uploadService.mergeChunk(uploadFileParamsDTO);

    }

    /**
     * @description: 上传分块文件
     * @param file
     * @param fileMd5
     * @param chunk
     * @return: com.example.douyin_publish.domain.dto.UploadFileResultDTO
     * @author zxm
     * @date: 2024/4/15 11:11
     */
    @PostMapping(value="/uploadchunk", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public UploadFileResultDTO uploadchunk(UploadVo uploadVo) throws Exception{
        UploadFileParamsDTO uploadFileParamsDto = new UploadFileParamsDTO(new DyMedia(),new DyPublish());
        String contentType = uploadVo.getFile().getContentType();
        uploadFileParamsDto.setContentType(contentType);
//        uploadFileParamsDto.setFileSize(uploadVo.getFile().getSize());
        uploadFileParamsDto.getDyMedia().setMd5(uploadVo.getMd5());
        uploadFileParamsDto.setChunks(uploadVo.getChunks());
        uploadFileParamsDto.setChunk(uploadVo.getChunk());
//        uploadFileParamsDto.getDyPublish().setAbout(uploadVo.getUid());
        return uploadService.uploadChunk(uploadFileParamsDto, uploadVo.getFile().getBytes());
    }

    /**
     * @param fileMd5
     * @Title: 判断文件是否上传过，是否存在分片，断点续传
     * @MethodName: checkBigFile
     * @Exception
     * @Description: 文件已存在，1
     * 文件没有上传过，0
     * 文件上传中断过，2 以及现在有的数组分片索引
     */
    @PostMapping(value = "/checkBigFile")
    public BaseResponse checkBigFile(@RequestBody CheckFileVo checkFileVo) {
        return uploadService.checkFile(checkFileVo.getFileMd5());
    }

    @PostMapping(value = "/downloadCreative")
    public BaseResponse downloadCreative(@RequestBody DownloadVo downloadVO){
        return uploadService.downloadCreative(downloadVO.getFileMd5());
    }

    @PostMapping(value = "/downloadCover")
    public BaseResponse downloadCover(@RequestBody DownloadVo downloadVO){
        return uploadService.downloadCover(downloadVO.getMediaId());
    }

    @PostMapping(value = "/uploadCover", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})   // comsume用来控制入参的数据类型
    // RequestParam一般用于name-valueString类型的请求域，RequestPart用于复杂的请求域.使用@RequestBody接收对象，所对应的content-type:application/json
    public UploadFileResultDTO upload(@RequestBody UploadVo uploadVo){
        // 将Vo的数据传给DTO
        UploadFileParamsDTO uploadFileParamsDto = new UploadFileParamsDTO(new DyMedia(),new DyPublish());
        String contentType = uploadVo.getFile().getContentType();
        uploadFileParamsDto.setContentType(contentType);
        uploadFileParamsDto.setFileSize(uploadVo.getFile().getSize());
        uploadFileParamsDto.getDyPublish().setAbout(uploadVo.getUid());
        uploadFileParamsDto.getDyPublish().setMediaId(uploadVo.getMediaId());

        if(contentType.indexOf("image")>=0){
            uploadFileParamsDto.getDyPublish().setType("001001");   //是个图片
        }
        else{
            uploadFileParamsDto.getDyPublish().setType("001003");   //是个视频
        }
        uploadFileParamsDto.getDyPublish().setFileName(uploadVo.getName());
        UploadFileResultDTO uploadFileResultDTO = null;
        try{
            uploadFileResultDTO = uploadService.uploadCoverFile(uploadFileParamsDto, uploadVo.getFile().getBytes());
        } catch (IOException e) {
            MsgException.cast("上传文件过程中出错");
        }
        return uploadFileResultDTO;
    }

    @PostMapping(value="editPublist")
    public UploadFileResultDTO edit(@RequestBody EditVo editVo){
        log.info("edit{}{}",editVo.getTitle(), editVo.getMediaId());
        return uploadService.editPublist(editVo);
    }

}
