package com.airondlph.economy.household;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;


/**
 * @author adriandlph / airondlph
 */
@SpringBootApplication
@Slf4j
public class HouseholdEconomyApplication {

	public static void main(String[] args) {
		log.info("Starting Household Economy application...");
		SpringApplication.run(HouseholdEconomyApplication.class, args);
		log.info("Application started!");
	}

	@PostConstruct
	public void init() {
		// Set timezone to UTC
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}


}
