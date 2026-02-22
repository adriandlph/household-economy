package com.airondlph.economy.household.data.model;

import com.airondlph.economy.household.data.VO;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author adriandlph / airondlph
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
public class BankCardVO implements VO, Serializable {

    protected Long id;
    protected Long cardNumber;
    protected Short ccv;
    protected Short pin;
    protected LocalDate expires;
    protected UserVO ownerVO;
    protected BankAccountVO bankAccountVO;

    @Override
    public String toString() {
        return new StringBuilder("BankCardVO{")
            .append("id=").append(getId())
            .append(", cardNumber=").append(getCardNumber())
            .append(", expires=").append(getExpires())
            .append(", owner=").append(getOwnerVO() == null ? null : getOwnerVO().getId())
            .append('}')
            .toString();
    }
}
