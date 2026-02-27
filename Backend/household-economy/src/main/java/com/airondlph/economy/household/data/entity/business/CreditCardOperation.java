package com.airondlph.economy.household.data.entity.business;

import com.airondlph.economy.household.data.model.CreditCardOperationVO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * @author adriandlph / airondlph
 */
@Entity(name = "CreditCardOperation")
@Table(name = "credit_card_operation")
@SuperBuilder
@ToString(callSuper = true)
@Inheritance(strategy = InheritanceType.JOINED)
public class CreditCardOperation extends Operation {

    @JoinColumn(name = "me", referencedColumnName = "id", nullable = false)
    @ManyToOne
    @Getter @Setter
    protected CreditCard me;
    @JoinColumn(name = "other", referencedColumnName = "id", nullable = false)
    @ManyToOne
    @Getter @Setter
    protected BankAccount other;

    @Override
    public CreditCardOperationVO getVO() {
        return addVOValues(CreditCardOperationVO.builder()).build();
    }

    protected <A extends CreditCardOperationVO.CreditCardOperationVOBuilder<?, ?>> A addVOValues(A builder) {
        super.addVOValues(builder)
            .me(getMe().getVO())
            .other(getOther().getVO());

        return builder;
    }

}