package com.airondlph.economy.household.data.model;

import lombok.*;

import java.time.LocalDate;

/**
 * @author adriandlph / airondlph
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class DebitCardVO extends BankCardVO {

    @Override
    public String toString() {
        return new StringBuilder("DebitCardVO{")
            .append("id=").append(getId())
            .append(", cardNumber=").append(getCardNumber())
            .append(", expires=").append(getExpires())
            .append(", owner=").append(getOwnerVO() == null ? null : getOwnerVO().getId())
            .append('}')
            .toString();
    }

    public static DebitCardVOBuilder builder() {
        return new DebitCardVOBuilder();
    }

    public static class DebitCardVOBuilder extends BankCardVOBuilder {
        private DebitCardVO data;

        public DebitCardVOBuilder() {
            data = new DebitCardVO();
        }

        public DebitCardVOBuilder id(Long id) {
            data.setId(id);
            return this;
        }

        public DebitCardVOBuilder cardNumber(Long cardNumber) {
            data.setCardNumber(cardNumber);
            return this;
        }

        public DebitCardVOBuilder ccv(Short ccv) {
            data.setCcv(ccv);
            return this;
        }
        public DebitCardVOBuilder pin(Short pin) {
            data.setPin(pin);
            return this;
        }

        public DebitCardVOBuilder expires(LocalDate expires) {
            data.setExpires(expires);
            return this;
        }

        public DebitCardVOBuilder ownerVO(UserVO ownerVO) {
            data.setOwnerVO(ownerVO);
            return this;
        }

        public DebitCardVOBuilder bankAccountVO(BankAccountVO bankAccountVO) {
            data.setBankAccountVO(bankAccountVO);
            return this;
        }

        public DebitCardVO build() {
            return data;
        }

    }

}
