package com.oran.oranpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.oran.oranpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class OranPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OranPictureBackendApplication.class, args);
    }

}
