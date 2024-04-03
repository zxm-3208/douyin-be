package com.example.be;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;

@SpringBootTest
class BeApplicationTests {

    @Test
    void contextLoads() {
        // 自定义生成JWT令牌的密钥
        String nacosSecret = "nacos_20240403_zxmzxm_nacos_token";
        // 输出密钥长度，要求不得低于32字符，否则无法启动节点。
        System.out.println("密钥长度》》》" + nacosSecret.length());
        // 密钥进行Base64编码
        byte[] data = nacosSecret.getBytes(StandardCharsets.UTF_8);
        System.out.println("密钥Base64编码》》》" + Base64Utils.encodeToString(data));
    }

}
