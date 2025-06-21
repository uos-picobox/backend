package com.uos.picobox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BePicoboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(BePicoboxApplication.class, args);
    }

}
