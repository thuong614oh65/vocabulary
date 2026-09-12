package com.thuong.vocabulary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VocabularyApplication {

	public static void main(String[] args) {
		SpringApplication.run(VocabularyApplication.class, args);
	}

}
