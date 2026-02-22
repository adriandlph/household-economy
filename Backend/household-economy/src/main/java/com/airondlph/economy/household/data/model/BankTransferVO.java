package com.airondlph.economy.household.data.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @author adriandlph / airondlph
 */
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
public class BankTransferVO extends OperationVO {

    protected BankAccountVO me;
    protected BankAccountVO other;

}
