package com.example.be.register.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

/**
 * (DyUser)实体类
 *
 * @author zxm
 * @since 2024-03-20 11:22:01
 */
@Data
@TableName(value = "dy_user")
public class DyUser implements Serializable {
    private static final long serialVersionUID = 546492953131891610L;
/**
     * 主键
     */
    private String id;
/**
     * 手机号码
     */
    private String phone;
/**
     * 密码，加密存储
     */
    private String password;

    /**
     * 验证码
     */
    private String code;

    /**
     * 昵称，默认是用户id
     */
    private String userName;
/**
     * 创建时间
     */
    private Date createTime;
/**
     * 更新时间
     */
    private Date updateTime;
/**
     * 邮箱地址
     */
    private String email;
/**
     * 最后登录的ip地址
     */
    private String loginIp;
/**
     * 最后登陆的时间
     */
    private Date loginTime;


}

