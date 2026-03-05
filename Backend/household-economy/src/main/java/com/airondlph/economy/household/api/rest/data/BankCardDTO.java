package com.airondlph.economy.household.api.rest.data;

import com.airondlph.economy.household.data.model.BankAccountVO;
import com.airondlph.economy.household.data.model.UserVO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author adriandlph / airondlph
 */
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
public class BankCardDTO implements Serializable {
    protected Long id;
    protected Long cardNumber;
    protected Short ccv;
    protected Short pin;
    protected LocalDate expires;
    protected UserDTO owner;
    protected BankAccountDTO bankAccount;
}
