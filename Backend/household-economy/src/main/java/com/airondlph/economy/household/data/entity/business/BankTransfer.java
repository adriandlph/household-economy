package com.airondlph.economy.household.data.entity.business;

import com.airondlph.economy.household.data.model.BankTransferVO;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * @author adriandlph / airondlph
 */
@Entity(name = "BankTransfer")
@Table(name = "bank_transfer")
@SuperBuilder
@ToString(callSuper = true)
public class BankTransfer extends Operation {

    @JoinColumn(name = "me", nullable = false)
    @ManyToMany
    @Getter @Setter
    protected BankAccount me;
    @JoinColumn(name = "other", nullable = false)
    @ManyToMany
    @Getter @Setter
    protected BankAccount other;

    @Override
    public BankTransferVO getVO() {
        return addVOValues(BankTransferVO.builder()).build();
    }

    protected <A extends BankTransferVO.BankTransferVOBuilder<?, ?>> A addVOValues(A builder) {
        super.addVOValues(builder)
            .me(getMe().getVO())
            .other(getOther().getVO());

        return builder;
    }

}
