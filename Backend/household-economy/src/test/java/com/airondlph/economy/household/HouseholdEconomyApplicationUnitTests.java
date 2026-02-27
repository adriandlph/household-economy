package com.airondlph.economy.household;

import com.airondlph.economy.household.controller.business.BusinessController;
import com.airondlph.economy.household.controller.data.Result;
import com.airondlph.economy.household.data.model.BankVO;
import com.airondlph.economy.household.data.model.UserVO;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest // Load test context
// @DataJpaTest // In-memory database
// @AutoConfigureTestDatabase(replace = Replace.NONE) // Use same database for testing
@ExtendWith(MockitoExtension.class)
// @ContextConfiguration(classes = HouseholdEconomyApplication.class)
class HouseholdEconomyApplicationUnitTests {

	@Autowired
	private EntityManager em;
	@Autowired
	private BusinessController businessController;


	@Test
	void Test1() {

		when(businessController.createBankVO(new UserVO(), BankVO.builder().build())).thenReturn(Result.create(-1)); // Force an error

		assertNotNull(1L);
		// assertEquals(1, 1);
	}

}
