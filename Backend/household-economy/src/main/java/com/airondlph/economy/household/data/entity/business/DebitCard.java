package com.airondlph.economy.household.data.entity.business;

import com.airondlph.economy.household.data.entity.user.User;
import com.airondlph.economy.household.data.model.BankAccountVO;
import com.airondlph.economy.household.data.model.DebitCardVO;
import com.airondlph.economy.household.data.model.UserVO;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * @author adriandlph / airondlph
 */
@Entity(name = "DebitCard")
@Table(name = "debit_card")
public class DebitCard extends BankCard {

    public static DebitCardBuilder builder() {
        return new DebitCardBuilder();
    }

    @Override
    public DebitCardVO getVO() {
        return DebitCardVO.builder()
            .id(getId())
            .cardNumber(getCardNumber())
            .ccv(getCcv())
            .pin(getPin())
            .expires(getExpires())
            .ownerVO(UserVO.builder().id(getOwner().getId()).build())
            .bankAccountVO(BankAccountVO.builder().id(getBankAccount().getId()).build())
            .build();
    }

    @Override
    public String toString() {
        return new StringBuilder("DebitCard{")
                .append("id=").append(getId())
                .append(", cardNumber=").append(getCardNumber())
                .append(", expires=").append(getExpires())
                .append('}')
                .toString();
    }

    public static class DebitCardBuilder extends BankCardBuilder {
        private DebitCard data;

        public DebitCardBuilder() {
            data = new DebitCard();
        }

        public DebitCardBuilder id(Long id) {
            data.setId(id);
            return this;
        }

        public DebitCardBuilder cardNumber(Long cardNumber) {
            data.setCardNumber(cardNumber);
            return this;
        }

        public DebitCardBuilder ccv(Short ccv) {
            data.setCcv(ccv);
            return this;
        }
        public DebitCardBuilder pin(Short pin) {
            data.setPin(pin);
            return this;
        }

        public DebitCardBuilder expires(LocalDate expires) {
            data.setExpires(expires);
            return this;
        }

        public DebitCardBuilder owner(User owner) {
            data.setOwner(owner);
            return this;
        }

        public DebitCardBuilder bankAccount(BankAccount bankAccount) {
            data.setBankAccount(bankAccount);
            return this;
        }

        public DebitCard build() {
            return data;
        }

    }

}
