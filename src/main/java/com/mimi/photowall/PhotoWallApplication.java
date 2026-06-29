package com.mimi.photowall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mimi.photowall.mapper")
public class PhotoWallApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhotoWallApplication.class, args);
    }

}
