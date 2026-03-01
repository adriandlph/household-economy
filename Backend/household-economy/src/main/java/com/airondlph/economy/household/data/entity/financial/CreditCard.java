package com.airondlph.economy.household.data.entity.financial;

import com.airondlph.economy.household.data.entity.user.User;
import com.airondlph.economy.household.data.model.BankAccountVO;
import com.airondlph.economy.household.data.model.CreditCardVO;
import com.airondlph.economy.household.data.model.UserVO;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * @author adriandlph / airondlph
 */
@Entity(name = "CreditCard")
@Table(name = "credit_card")
public class CreditCard extends BankCard {

    public static CreditCardBuilder builder() {
        return new CreditCardBuilder();
    }

    @Override
    public CreditCardVO getVO() {
        return CreditCardVO.builder()
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
        return new StringBuilder("CreditCard{")
            .append("id=").append(getId())
            .append(", cardNumber=").append(getCardNumber())
            .append(", expires=").append(getExpires())
            .append('}')
            .toString();
    }

    public static class CreditCardBuilder extends BankCardBuilder {
        private CreditCard data;

        public CreditCardBuilder() {
            data = new CreditCard();
        }

        public CreditCardBuilder id(Long id) {
            data.setId(id);
            return this;
        }

        public CreditCardBuilder cardNumber(Long cardNumber) {
            data.setCardNumber(cardNumber);
            return this;
        }

        public CreditCardBuilder ccv(Short ccv) {
            data.setCcv(ccv);
            return this;
        }
        public CreditCardBuilder pin(Short pin) {
            data.setPin(pin);
            return this;
        }

        public CreditCardBuilder expires(LocalDate expires) {
            data.setExpires(expires);
            return this;
        }

        public CreditCardBuilder owner(User owner) {
            data.setOwner(owner);
            return this;
        }

        public CreditCardBuilder bankAccount(BankAccount bankAccount) {
            data.setBankAccount(bankAccount);
            return this;
        }

        public CreditCard build() {
            return data;
        }

    }

}
