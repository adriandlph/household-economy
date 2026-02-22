package com.airondlph.economy.household.data.model;

import lombok.*;

import java.time.LocalDate;

/**
 * @author adriandlph / airondlph
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class CreditCardVO extends BankCardVO {

    @Override
    public String toString() {
        return new StringBuilder("CreditCardVO{")
            .append("id=").append(getId())
            .append(", cardNumber=").append(getCardNumber())
            .append(", expires=").append(getExpires())
            .append(", owner=").append(getOwnerVO() == null ? null : getOwnerVO().getId())
            .append('}')
            .toString();
    }

    public static CreditCardVOBuilder builder() {
        return new CreditCardVOBuilder();
    }

    public static class CreditCardVOBuilder extends BankCardVOBuilder {
        private CreditCardVO data;

        public CreditCardVOBuilder() {
            data = new CreditCardVO();
        }

        public CreditCardVOBuilder id(Long id) {
            data.setId(id);
            return this;
        }

        public CreditCardVOBuilder cardNumber(Long cardNumber) {
            data.setCardNumber(cardNumber);
            return this;
        }

        public CreditCardVOBuilder ccv(Short ccv) {
            data.setCcv(ccv);
            return this;
        }
        public CreditCardVOBuilder pin(Short pin) {
            data.setPin(pin);
            return this;
        }

        public CreditCardVOBuilder expires(LocalDate expires) {
            data.setExpires(expires);
            return this;
        }

        public CreditCardVOBuilder ownerVO(UserVO ownerVO) {
            data.setOwnerVO(ownerVO);
            return this;
        }

        public CreditCardVOBuilder bankAccountVO(BankAccountVO bankAccountVO) {
            data.setBankAccountVO(bankAccountVO);
            return this;
        }

        public CreditCardVO build() {
            return data;
        }

    }

}
