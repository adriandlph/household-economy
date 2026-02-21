package com.airondlph.economy.household.data.entity;

import com.airondlph.economy.household.data.model.FinancialBusinessVO;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * @author adriandlph / airondlph
 */
@Entity(name = "FinancialBusiness")
@Table(name = "financial_business")
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class FinancialBusiness extends Business {

    @Override
    public FinancialBusinessVO getVO() {
        return FinancialBusinessVO.builder()
            .id(getId())
            .name(getName())
            .build();
    }

    @Override
    public String toString() {
        return new StringBuilder("FinancialBusiness{")
                .append("id=").append(getId())
                .append(", name=").append(getName())
                .append('}')
                .toString();
    }

    public static FinancialBusinessBuilder builder() {
        return new FinancialBusinessBuilder();
    }

    public static class FinancialBusinessBuilder extends BusinessBuilder {
        private FinancialBusiness data;

        public FinancialBusinessBuilder() {
            data = new FinancialBusiness();
        }

        public FinancialBusinessBuilder id(Long id) {
            data.setId(id);
            return this;
        }

        public FinancialBusinessBuilder name(String name) {
            data.setName(name);
            return this;
        }

        public FinancialBusiness build() {
            return data;
        }

    }

}
