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
public class DebitCardOperationVO extends OperationVO {

    protected DebitCardVO me;
    protected BankAccountVO other;

}
