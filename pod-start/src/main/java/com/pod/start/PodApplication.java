package com.pod.start;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.pod")
@MapperScan("com.pod.**.mapper")
public class PodApplication {
    public static void main(String[] args) {
        SpringApplication.run(PodApplication.class, args);
    }
}
