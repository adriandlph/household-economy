package com.airondlph.economy.household;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


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


}
