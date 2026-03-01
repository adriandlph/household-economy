package com.airondlph.economy.household;

import com.airondlph.economy.household.logic.financial.FinancialController;
import com.airondlph.economy.household.data.model.BankVO;
import com.airondlph.economy.household.data.model.UserVO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.TimeZone;


/**
 * @author adriandlph / airondlph
 */
@SpringBootApplication
@Slf4j
public class HouseholdEconomyApplication implements CommandLineRunner {
	private static ConfigurableApplicationContext ctx;

	@Autowired
	private FinancialController businessController;

	public static void main(String[] args) {
		log.info("Starting Household Economy application...");
		ctx = SpringApplication.run(HouseholdEconomyApplication.class, args);
		log.info("Application started!");
	}

	@PostConstruct
	public void init() {
		// Set timezone to UTC
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	@Override
	public void run(String... args) throws Exception {

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			mainMenu(in);

			SpringApplication.exit(ctx, () -> 0);

		} catch (Exception ex) {
			log.error("Error with input...");
			log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());

			SpringApplication.exit(ctx, () -> 1);
		}
	}

	private void mainMenu(BufferedReader in) throws Exception {
		String buffer;


		log.info("\nMain menu: \n" +
				"---------------------\n" +
				" - bank\n" +
				" - exit\n");


		while (true) {
			buffer = in.readLine();

			if (buffer == null || buffer.isBlank()) continue;
			if (buffer.equalsIgnoreCase("exit")) break;

			if (buffer.equalsIgnoreCase("bank")) {
				bankMenu(in);
				continue;
			}

			log.error("Invalid command");
		}
	}

	private void bankMenu(BufferedReader in) throws Exception {
		String buffer;

		log.info("\nBank menu: \n" +
				"---------------------\n" +
				" - create <name>\n" +
				" - get <id>\n" +
				" - delete <id>\n" +
				" - edit <id> <name>\n" +
				" - return\n");


		while (true) {
			buffer = in.readLine();

			if (buffer == null || buffer.isBlank()) continue;
			if (buffer.equalsIgnoreCase("return")) break;

			if (buffer.toLowerCase().contains("create ")) {
				String name = buffer.replace("create ", "");
				if (name.equalsIgnoreCase("null")) name = null;
				businessController.createBankVO(UserVO.builder().id(1L).build(), BankVO.builder().name(name).build());
			}

			if (buffer.contains("get ")) {
				String id = buffer.replace("get ", "");
				log.info("Bank: {}", businessController.getBankByIdVO(UserVO.builder().id(1L).build(), BankVO.builder().id(Long.parseLong(id)).build()).toString());
			}

			if (buffer.contains("delete ")) {
				String id = buffer.replace("delete ", "");
				log.info("Bank: {}", businessController.deleteBankByIdVO(UserVO.builder().id(1L).build(), BankVO.builder().id(Long.parseLong(id)).build()).toString());
			}

			if (buffer.contains("edit ")) {
				String data[] = buffer.replace("edit ", "").split(" ");
				log.info("Bank: {}", businessController.editBankVO(UserVO.builder().id(1L).build(), BankVO.builder().id(Long.parseLong(data[0])).name(data[1]).build()).toString());
			}

		}
	}
}
