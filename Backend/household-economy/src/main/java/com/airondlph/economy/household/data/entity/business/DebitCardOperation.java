package com.airondlph.economy.household.data.entity.business;

import com.airondlph.economy.household.data.model.DebitCardOperationVO;
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
@Entity(name = "DebitCardOperation")
@Table(name = "debit_card_operation")
@SuperBuilder
@ToString(callSuper = true)
public class DebitCardOperation extends Operation {

    @JoinColumn(name = "me", nullable = false)
    @ManyToMany
    @Getter @Setter
    protected DebitCard me;
    @JoinColumn(name = "other", nullable = false)
    @ManyToMany
    @Getter @Setter
    protected BankAccount other;

    @Override
    public DebitCardOperationVO getVO() {
        return addVOValues(DebitCardOperationVO.builder()).build();
    }

    protected <A extends DebitCardOperationVO.DebitCardOperationVOBuilder<?, ?>> A addVOValues(A builder) {
        super.addVOValues(builder)
                .me(getMe().getVO())
                .other(getOther().getVO());

        return builder;
    }

}