package com.example.douyin_chat.entity;

import lombok.Data;

import java.util.List;

/**
 * @author : zxm
 * @date: 2024/6/7 - 21:51
 * @Description: com.example.douyin_chat.entity
 * @version: 1.0
 */
@Data
public class LoginBack
{

    List<ImNode> imNodeList;

    private String token;

    private ChatUserDTO userDTO;

}