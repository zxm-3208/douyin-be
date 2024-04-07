package com.example.douyin_gateway.filter;

import com.example.douyin_gateway.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author : zxm
 * @date: 2024/4/7 - 21:35
 * @Description: com.example.douyin_gateway.filter
 * @version: 1.0
 */
@Component
@Slf4j
public class AuthorizeFilter implements Ordered, GlobalFilter {

    @Value("${token.header}")
    private String header;

    @Value("${token.secret}")
    private String secret;

    @Value("${token.expireTime}")
    private int expireTime;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 获取request和response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 2.判断是否登录
        String uriPath = (request.getURI().getPath());
        System.out.println(uriPath);
        if(uriPath.contains("/code")||uriPath.contains("/login")||uriPath.contains("/captchaImage")||uriPath.contains("/logout")||uriPath.contains("/logoinbyusername")){
            return chain.filter(exchange);
        }
        // 3. 获取token
        String token = request.getHeaders().getFirst("authorization");
        // 4. 判断token是否存在
        if(StringUtil.isBlank(token)){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 5. 判断token是否有效
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            int result = JwtUtil.verifyToken(claims);
            if (result == 1 || result == 2) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        }catch (Exception e){
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 6. 放行
        return chain.filter(exchange);
    }

    /**
     * 优先级设置，值越小，优先级越高
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
