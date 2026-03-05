package com.airondlph.economy.household.data.entity.financial;

import com.airondlph.economy.household.data.HasVO;
import com.airondlph.economy.household.data.enumeration.Currency;
import com.airondlph.economy.household.data.model.BankAccountVO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author adriandlph / airondlph
 */
@Entity(name = "BankAccount")
@Table(name = "bank_account")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BankAccount implements HasVO, Serializable {

    public static final int BANK_ACCOUNT_NUMBER_MAX_LENGTH = 255;

    @Column(name = "id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;
    @Column(name = "bank_account_number", length = BANK_ACCOUNT_NUMBER_MAX_LENGTH, nullable = false)
    @Getter @Setter
    private String bankAccountNumber;
    @Column(name = "balance", nullable = false)
    @Getter @Setter
    private Long balance = 0L;
    @Column(name = "currency", nullable = false)
    @Getter @Setter
    private Currency currency;
    @Column(name = "last_update", nullable = false)
    @Getter @Setter
    private LocalDateTime lastUpdate;
    @JoinColumn(name = "bank_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @Getter @Setter
    @ToString.Exclude
    private Bank bank;

    @Override
    public BankAccountVO getVO() {
        return BankAccountVO.builder()
            .id(getId())
            .bankAccountNumber(getBankAccountNumber())
            .balance(getBalance())
            .currency(getCurrency())
            .lastUpdate(getLastUpdate())
            .build();
    }

}
