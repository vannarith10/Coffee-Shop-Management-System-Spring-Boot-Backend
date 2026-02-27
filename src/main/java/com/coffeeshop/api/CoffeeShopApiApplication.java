package com.coffeeshop.api;

import com.coffeeshop.api.config.payment_test.PaywayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PaywayProperties.class)
public class CoffeeShopApiApplication {

	public static void main(String[] args) {

		SpringApplication.run(CoffeeShopApiApplication.class, args);

	}
}
