package com.example.douyin_user.domain.vo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author : zxm
 * @date: 2024/5/14 - 9:40
 * @Description: com.example.douyin_user.domain.vo
 * @version: 1.0
 */
@Data
public class SubmitEditVO {
    private MultipartFile file;
    private String editName;
    private String userId;
    private String editIntro;
    private String gender;
    private String birthday;
    private String defaultIcon;
}
