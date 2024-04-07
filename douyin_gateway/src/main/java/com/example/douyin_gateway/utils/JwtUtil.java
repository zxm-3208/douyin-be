package com.example.douyin_gateway.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

/**
 * @author : zxm
 * @date: 2024/4/7 - 21:53
 * @Description: com.example.douyin_gateway.utils
 * @version: 1.0
 */
public class JwtUtil {

    // 最小刷新间隔(S)
    private static final int REFRESH_TIME = 300;

    public static int verifyToken(Claims claims) {
        if(claims==null){
            return 1;
        }
        try {
            claims.getExpiration()
                    .before(new Date());
            // 需要自动刷新TOKEN
            if((claims.getExpiration().getTime()-System.currentTimeMillis())>REFRESH_TIME*1000){
                return -1;
            }else {
                return 0;
            }
        } catch (ExpiredJwtException ex) {
            return 1;
        }catch (Exception e){
            return 2;
        }
    }
}
