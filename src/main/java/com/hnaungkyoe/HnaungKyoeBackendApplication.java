package com.hnaungkyoe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HnaungKyoeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HnaungKyoeBackendApplication.class, args);
    }

}
