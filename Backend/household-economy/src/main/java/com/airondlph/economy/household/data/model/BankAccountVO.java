package com.airondlph.economy.household.data.model;

import com.airondlph.economy.household.data.VO;
import com.airondlph.economy.household.data.entity.business.Bank;
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
public class BankAccountVO implements VO, Serializable {

    private Long id;
    private String bankAccountNumber;
    private Long balance;
    private Currency currency;
    private LocalDateTime lastUpdate;
    private Bank bank;

    @Override
    public String toString() {
        return new StringBuilder("BankAccountVO{")
            .append("id=").append(getId())
            .append(", bankAccountNumber=").append(getBankAccountNumber())
            .append(", balance=").append(getBalance())
            .append(", currency=").append(getCurrency().name())
            .append(", lastUpdate=").append(getLastUpdate())
            .append(", bank=").append(getBank())
            .append('}')
            .toString();
    }

}
