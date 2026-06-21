package com.csdl.access;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * He thong quan ly truy cap CSDL.
 *
 * Spring Boot + Thymeleaf monolith theo ADR 0001.
 */
@SpringBootApplication
public class CsdlAccessApplication {

    public static void main(String[] args) {
        SpringApplication.run(CsdlAccessApplication.class, args);
    }
}
