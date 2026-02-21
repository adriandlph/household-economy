package com.airondlph.economy.household.data.model;

import lombok.*;

/**
 * @author adriandlph / airondlph
 */
@Builder
@Getter
@Setter
public class BankVO extends FinancialBusinessVO {

    protected BankVO() {

    }

    protected BankVO(Long id, String name) {
        super(id, name);
    }

    @Override
    public String toString() {
        return new StringBuilder("BankVO{")
            .append("id=").append(getId())
            .append(", name=").append(getName())
            .append('}')
            .toString();
    }

    public static BankVOBuilder builder() {
        return new BankVOBuilder();
    }

    public static class BankVOBuilder extends FinancialBusinessVOBuilder {
        private BankVO data;

        public BankVOBuilder() {
            data = new BankVO();
        }

        public BankVOBuilder id(Long id) {
            data.setId(id);
            return this;
        }

        public BankVOBuilder name(String name) {
            data.setName(name);
            return this;
        }

        public BankVO build() {
            return data;
        }

    }

}
