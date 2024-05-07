package com.example.douyin_commons.utils;

import com.example.douyin_commons.core.domain.UserDTO;
import org.apache.catalina.User;

/**
 * @author : zxm
 * @date: 2024/5/7 - 11:47
 * @Description: com.example.douyin_commons.utils
 * @version: 1.0
 */
public class UserHolder {

    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }


}
