package com.airondlph.economy.household.data.entity.business;

import com.airondlph.economy.household.data.HasVO;
import com.airondlph.economy.household.data.enumeration.Currency;
import com.airondlph.economy.household.data.enumeration.OperationType;
import com.airondlph.economy.household.data.model.OperationVO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author adriandlph / airondlph
 */
@Entity(name = "Operation")
@Table(name = "operation")
@SuperBuilder
@ToString(callSuper = true)
@Inheritance(strategy = InheritanceType.JOINED)
public class Operation implements HasVO, Serializable {

    public static final int CONCEPT_MAX_LENGTH = 255;
    public static final int DESCRIPTION_MAX_LENGTH = 2048;

    @Column(name = "id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    protected Long id;
    @Column(name = "concept", length = CONCEPT_MAX_LENGTH)
    @Getter @Setter
    protected String concept;
    @Column(name = "description", length = DESCRIPTION_MAX_LENGTH)
    @Getter @Setter
    protected String description;
    @Column(name = "value")
    @Getter @Setter
    protected Long value = 0L; // in deciCurrency (value = currencyValue / 100)
    @Column(name = "from_currency")
    @Getter @Setter
    protected Currency fromCurrency;
    @Column(name = "to_currency")
    @Getter @Setter
    protected Currency toCurrency;
    @Column(name = "conversion")
    @Getter @Setter
    protected Float conversion = 1F;
    @Column(name = "made_when", nullable = false)
    @Getter @Setter
    protected LocalDateTime madeWhen;
    @Column(name = "apply_when", nullable = false)
    @Getter @Setter
    protected LocalDateTime applyWhen;
    @Column(name = "operation_type", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @Getter @Setter
    protected OperationType operationType;

    @Override
    public OperationVO getVO() {
        return addVOValues(OperationVO.builder()).build();
    }

    protected <A extends OperationVO.OperationVOBuilder<?, ?>> A addVOValues(A builder) {
        builder
            .id(getId())
            .concept(getConcept())
            .description(getDescription())
            .value(getValue())
            .fromCurrency(getFromCurrency())
            .toCurrency(getToCurrency())
            .conversion(getConversion())
            .madeWhen(getMadeWhen())
            .applyWhen(getApplyWhen());

        return builder;
    }

}
