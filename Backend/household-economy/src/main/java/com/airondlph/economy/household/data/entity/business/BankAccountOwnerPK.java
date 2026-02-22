package com.airondlph.economy.household.data.entity.business;

import com.airondlph.economy.household.data.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author adriandlph / airondlph
 */
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class BankAccountOwnerPK implements Serializable {

    @ManyToOne
    @JoinColumn(name = "bank_account_id", referencedColumnName = "id", nullable = false)
    private BankAccount bankAccount;
    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = false)
    private User owner;

    @Override
    public String toString() {
        return new StringBuilder("BankAccountOwnerPK{")
            .append("bankAccount=").append(getBankAccount())
            .append(", owner=").append(getOwner())
            .append('}')
            .toString();
    }

}
