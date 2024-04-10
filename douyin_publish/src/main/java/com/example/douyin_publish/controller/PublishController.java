package com.example.douyin_publish.controller;

import com.example.douyin_commons.core.exception.MsgException;
import com.example.douyin_publish.domain.dto.UploadFileParamsDTO;
import com.example.douyin_publish.domain.dto.UploadFileResultDTO;
import com.example.douyin_publish.domain.po.DyMedia;
import com.example.douyin_publish.domain.po.DyPublish;
import com.example.douyin_publish.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author : zxm
 * @date: 2024/4/10 - 19:27
 * @Description: com.example.douyin_publish.controller
 * @version: 1.0
 */

@RestController     //@Controller + @ResponseBody
@RequestMapping("/publish")
public class PublishController {

    @Autowired
    UploadService uploadService;

    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})   // comsume用来控制入参的数据类型
    // RequestParam一般用于name-valueString类型的请求域，RequestPart用于复杂的请求域
    public UploadFileResultDTO upload(@RequestPart("filedata") MultipartFile filedata,
                                      @RequestParam(value = "folder", required = false) String folder,
                                      @RequestParam(value = "objectName", required = false) String objectName){
        UploadFileParamsDTO uploadFileParamsDto = new UploadFileParamsDTO(new DyMedia(),new DyPublish());
        String contentType = filedata.getContentType();
        uploadFileParamsDto.setContentType(contentType);
        uploadFileParamsDto.setFileSize(filedata.getSize());
        if(contentType.indexOf("image")>=0){
            uploadFileParamsDto.getDyPublish().setType("001001");   //是个图片
        }
        else{
            uploadFileParamsDto.getDyPublish().setType("001003");   //是个视频
        }
        uploadFileParamsDto.getDyPublish().setTitle(filedata.getOriginalFilename());
        UploadFileResultDTO uploadFileResultDTO = null;
        try{
            uploadFileResultDTO = uploadService.uploadFile(uploadFileParamsDto, filedata.getBytes(), folder, objectName);
        } catch (IOException e) {
            MsgException.cast("上传文件过程中出错");
        }
        return uploadFileResultDTO;
    }

}
