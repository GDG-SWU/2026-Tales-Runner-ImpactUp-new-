package com.example.dual_tales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DualTalesApplication {

	public static void main(String[] args) {
		SpringApplication.run(DualTalesApplication.class, args);
	}

}
