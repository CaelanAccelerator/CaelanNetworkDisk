package com.disk.files;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.disk.files", "com.disk.storage", "com.disk.web"})
@MapperScan("com.disk.files.infrastructure.mapper")
public class FilesApplication {
    public static void main(String[] args) {
        SpringApplication.run(FilesApplication.class, args);
    }
}
