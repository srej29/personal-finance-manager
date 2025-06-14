package com.finance; // Updated package name

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
// Ensure all components within the 'com.finance' base package are scanned.
// This is important for Spring to find all your controllers, services, and repositories.
// @ComponentScan(basePackages = "com.finance")
public class PersonalFinanceManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonalFinanceManagerApplication.class, args);
	}

}
