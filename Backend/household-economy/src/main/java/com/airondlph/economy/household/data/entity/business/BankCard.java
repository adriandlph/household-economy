package com.airondlph.economy.household.data.entity.business;

import com.airondlph.economy.household.data.HasVO;
import com.airondlph.economy.household.data.VO;
import com.airondlph.economy.household.data.entity.user.User;
import com.airondlph.economy.household.data.model.BankAccountVO;
import com.airondlph.economy.household.data.model.BankCardVO;
import com.airondlph.economy.household.data.model.UserVO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author adriandlph / airondlph
 */
@Entity(name = "BankCard")
@Table(name = "bank_card")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Inheritance(strategy = InheritanceType.JOINED)
public class BankCard implements HasVO, Serializable {

    @Column(name = "id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    protected Long id;
    @Column(name = "card_number")
    @Getter @Setter
    protected Long cardNumber;
    @Column(name = "ccv")
    @Getter @Setter
    protected Short ccv;
    @Column(name = "pin")
    @Getter @Setter
    protected Short pin;
    @Column(name = "expires")
    @Getter @Setter
    protected LocalDate expires;
    @JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = false)
    @ManyToOne
    @Getter @Setter
    protected User owner;
    @JoinColumn(name = "bank_account_id", referencedColumnName = "id", nullable = false)
    @ManyToOne
    @Getter @Setter
    protected BankAccount bankAccount;

    @Override
    public BankCardVO getVO() {
        return BankCardVO.builder()
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
        return new StringBuilder("BankCard{")
            .append("id=").append(getId())
            .append(", cardNumber=").append(getCardNumber())
            .append(", expires=").append(getExpires())
            .append('}')
            .toString();
    }

}
