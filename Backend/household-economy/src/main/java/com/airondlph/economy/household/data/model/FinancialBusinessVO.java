package com.airondlph.economy.household.data.model;

import lombok.*;

/**
 * @author adriandlph / airondlph
 */
@Getter
@Setter
public class FinancialBusinessVO extends BusinessVO {

    protected FinancialBusinessVO() {

    }

    protected FinancialBusinessVO(Long id, String name) {
        super(id, name);
    }

    @Override
    public String toString() {
        return new StringBuilder("FinancialBusinessVO{")
            .append("id=").append(getId())
            .append(", name=").append(getName())
            .append('}')
            .toString();
    }

    public static FinancialBusinessVOBuilder builder() {
        return new FinancialBusinessVOBuilder();
    }

    public static class FinancialBusinessVOBuilder extends BusinessVOBuilder {
        private FinancialBusinessVO data;

        public FinancialBusinessVOBuilder() {
            data = new FinancialBusinessVO();
        }

        public FinancialBusinessVOBuilder id(Long id) {
            data.setId(id);
            return this;
        }

        public FinancialBusinessVOBuilder name(String name) {
            data.setName(name);
            return this;
        }

        public FinancialBusinessVO build() {
            return data;
        }

    }

}
