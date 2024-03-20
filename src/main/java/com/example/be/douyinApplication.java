package com.example.be;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author : zxm
 * @date: 2024/3/19 - 03 - 19 - 20:04
 * @Description: com.example.be
 * @version: 1.0
 */
@SpringBootApplication
@MapperScan("com.example.be.**.mapper")
public class douyinApplication {

    public static void main(String[] args) {
        SpringApplication.run(douyinApplication.class, args);
        System.out.println("启动成功！");
    }

}
