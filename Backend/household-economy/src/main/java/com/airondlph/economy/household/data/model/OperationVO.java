package com.airondlph.economy.household.data.model;

import com.airondlph.economy.household.data.VO;
import com.airondlph.economy.household.data.enumeration.Currency;
import com.airondlph.economy.household.data.enumeration.OperationType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author adriandlph / airondlph
 */
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
public class OperationVO implements VO, Serializable {

    protected Long id;
    protected String concept;
    protected String description;
    protected Long value; // in deciCurrency (value = currencyValue / 100)
    protected Currency fromCurrency;
    protected Currency toCurrency;
    protected Float conversion;
    protected LocalDateTime madeWhen;
    protected LocalDateTime applyWhen;
    protected OperationType operationType;

}
