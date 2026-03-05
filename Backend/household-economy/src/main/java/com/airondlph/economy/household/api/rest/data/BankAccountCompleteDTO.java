package com.airondlph.economy.household.api.rest.data;

import com.airondlph.economy.household.data.enumeration.Currency;
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
public class BankAccountCompleteDTO implements Serializable {

    private Long id;
    private String bankAccountNumber;
    private Long balance;
    private Currency currency;
    private BankDTO bank;
    private List<UserDTO> owners;

}
