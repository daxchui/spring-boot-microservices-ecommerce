package com.comp5348.deliveryco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DeliveryCoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeliveryCoApplication.class, args);
	}

}
