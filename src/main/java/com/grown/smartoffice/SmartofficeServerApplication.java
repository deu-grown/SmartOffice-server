package com.grown.smartoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SmartofficeServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartofficeServerApplication.class, args);
	}

}
