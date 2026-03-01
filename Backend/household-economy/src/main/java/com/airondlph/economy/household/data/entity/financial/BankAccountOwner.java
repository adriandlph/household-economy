package com.airondlph.economy.household.data.entity.financial;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * @author adriandlph / airondlph
 */
@Entity(name = "BankAccountOwner")
@Table(name = "bank_account_owner")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
public class BankAccountOwner implements Serializable {

    @EmbeddedId
    @Column(name = "id")
    private BankAccountOwnerPK id;

    @Override
    public String toString() {
        return new StringBuilder("BankAccountOwner{")
            .append("id=").append(getId())
            .append('}')
            .toString();
    }
}
