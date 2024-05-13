package com.example.douyin_commons.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author : zxm
 * @date: 2024/5/13 - 17:15
 * @Description: com.example.douyin_user.domain.dto
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditDTO {

    private String editName;
    private String userId;
    private String editIntro;
    private String gender;
    private Date birthday;

}
