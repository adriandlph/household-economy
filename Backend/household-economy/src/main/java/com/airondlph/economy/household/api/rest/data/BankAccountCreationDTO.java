package com.airondlph.economy.household.api.rest.data;

import com.airondlph.economy.household.data.enumeration.Currency;
import com.airondlph.economy.household.data.model.BankVO;
import com.airondlph.economy.household.data.model.UserVO;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author adriandlph / airondlph
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class BankAccountCreationDTO implements Serializable {

    private String bankAccountNumber;
    private Currency currency;
    private BankDTO bank;
    private List<UserDTO> owners;

}
