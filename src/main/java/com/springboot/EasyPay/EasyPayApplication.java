package com.springboot.EasyPay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class EasyPayApplication {

	public static void main(String[] args) {
		SpringApplication.run(EasyPayApplication.class, args);
	}

}
