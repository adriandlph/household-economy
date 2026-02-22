 package com.airondlph.economy.household.data.entity.business;

import com.airondlph.economy.household.data.model.BankVO;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

 /**
 * @author adriandlph / airondlph
 */
@Entity(name = "Bank")
@Table(name = "bank")
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class Bank extends FinancialBusiness {

    @Override
    public BankVO getVO() {
        return BankVO.builder()
            .id(getId())
            .name(getName())
            .build();
    }

    @Override
    public String toString() {
        return new StringBuilder("Bank{")
            .append("id=").append(getId())
            .append(", name=").append(getName())
            .append('}')
            .toString();
    }

    public static BankBuilder builder() {
        return new BankBuilder();
    }

    public static class BankBuilder extends FinancialBusinessBuilder {
        private Bank data;

        public BankBuilder() {
            data = new Bank();
        }

        public BankBuilder id(Long id) {
            data.setId(id);
            return this;
        }

        public BankBuilder name(String name) {
            data.setName(name);
            return this;
        }

        public Bank build() {
            return data;
        }

    }

}
