package com.example.douyin_chat.server.session.dao.service;

import com.example.douyin_chat.server.session.dao.SessionCacheDAO;
import com.example.douyin_chat.server.session.dao.UserCacheDAO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author : zxm
 * @date: 2024/5/31 - 21:19
 * @Description: com.example.douyin_chat.server.session
 * @version: 1.0
 */
@Slf4j
@Data
@Repository
public class SessionManger {

    @Autowired
    UserCacheDAO userCacheDAO;

    @Autowired
    SessionCacheDAO sessionCacheDAO;

}
