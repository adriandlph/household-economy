package com.airondlph.economy.household.data.entity.business;

import com.airondlph.economy.household.data.model.BankTransferVO;
import jakarta.persistence.*;
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
@Inheritance(strategy = InheritanceType.JOINED)
public class BankTransfer extends Operation {

    @JoinColumn(name = "me_id", referencedColumnName = "id", nullable = false)
    @ManyToOne
    @Getter @Setter
    protected BankAccount me;
    @JoinColumn(name = "other_id", referencedColumnName = "id", nullable = false)
    @ManyToOne
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
