package com.airondlph.economy.household.data.model;

import com.airondlph.economy.household.data.VO;
import com.airondlph.economy.household.data.enumeration.Currency;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author adriandlph / airondlph
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString(callSuper = true)
public class BankAccountVO implements VO, Serializable {

    private Long id;
    private String bankAccountNumber;
    private Long balance;
    private Currency currency;
    private LocalDateTime lastUpdate;
    private BankVO bankVO;

    public BankAccountVO(BankAccountVO bankAccountVO) {
        this.id = bankAccountVO.id; // Long is immutable
        this.bankAccountNumber = bankAccountVO.bankAccountNumber; // String is immutable
        this.balance = bankAccountVO.balance; // Long is immutable
        this.currency = bankAccountVO.currency; // Enums are immutable
        this.lastUpdate = bankAccountVO.lastUpdate; // LocalDateTime is immutable
        this.bankVO = new BankVO(bankAccountVO.bankVO); // Creates a copy
    }

}
