package com.kaoshi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.kaoshi.**.mapper")
@SpringBootApplication
public class KaoshiApplication {
    public static void main(String[] args) {
        SpringApplication.run(KaoshiApplication.class, args);
    }
}

