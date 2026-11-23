package com.gestaomedicamentos.beckend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BeckendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeckendApplication.class, args);
	}
    
}

