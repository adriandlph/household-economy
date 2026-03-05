package com.airondlph.economy.household.api.rest.data;

import com.airondlph.economy.household.data.enumeration.Currency;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author adriandlph / airondlph
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankAccountCreationResultDTO implements Serializable {
    private Long id;
    private String bankAccountNumber;
    private Long balance;
    private Currency currency;
}
