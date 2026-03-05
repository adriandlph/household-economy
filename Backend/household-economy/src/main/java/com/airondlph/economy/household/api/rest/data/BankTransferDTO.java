package com.airondlph.economy.household.api.rest.data;

import com.airondlph.economy.household.data.enumeration.Currency;
import com.airondlph.economy.household.data.enumeration.OperationType;
import com.airondlph.economy.household.data.model.BankAccountVO;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author adriandlph / airondlph
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class BankTransferDTO implements Serializable {

    protected Long id;
    protected String concept;
    protected String description;
    protected OperationType operationType;
    protected Long value; // in deciCurrency (value = currencyValue / 100)
    protected Currency fromCurrency;
    protected Currency toCurrency;
    protected Float conversion;
    protected LocalDateTime madeWhen;
    protected LocalDateTime applyWhen;
    protected BankAccountDTO me;
    protected BankAccountDTO other;

}
