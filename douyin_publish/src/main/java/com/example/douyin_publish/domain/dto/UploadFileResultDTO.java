package com.example.douyin_publish.domain.dto;

import com.example.douyin_commons.core.domain.ResultCode;
import com.example.douyin_publish.domain.po.DyMedia;
import lombok.Data;

/**
 * @author : zxm
 * @date: 2024/4/9 - 12:12
 * @Description: com.example.douyin_publish.domain.dto
 * @version: 1.0
 */
@Data
public class UploadFileResultDTO extends DyMedia {
    private int chunkIndex;
    private int resultCode;
    private String resultMsg;

    public UploadFileResultDTO() {
    }


    public static UploadFileResultDTO successMerge(){
        UploadFileResultDTO resultDTO = new UploadFileResultDTO();
        resultDTO.setResultCode(ResultCode.SUCCESS.getCode());
        resultDTO.setResultMsg(ResultCode.SUCCESS.getMessage());
        return resultDTO;
    }

    public static UploadFileResultDTO successIndex(int chunkIndex) {
        UploadFileResultDTO resultDTO = new UploadFileResultDTO();
        resultDTO.setChunkIndex(chunkIndex);
        resultDTO.setResultCode(ResultCode.SUCCESS.getCode());
        resultDTO.setResultMsg(ResultCode.SUCCESS.getMessage());
        return resultDTO;
    }

    public static UploadFileResultDTO failIndex(int chunkIndex) {
        UploadFileResultDTO resultDTO = new UploadFileResultDTO();
        resultDTO.setChunkIndex(chunkIndex);
        resultDTO.setResultCode(ResultCode.Error.getCode());
        resultDTO.setResultMsg(ResultCode.Error.getMessage());
        return resultDTO;
    }
}
