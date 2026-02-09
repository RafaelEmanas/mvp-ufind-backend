package com.ufind.ufindapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class UfindappApplication {

	public static void main(String[] args) {
		SpringApplication.run(UfindappApplication.class, args);
	}

}
