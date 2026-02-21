package com.airondlph.economy.household.api.rest.data;

import lombok.*;

import java.io.Serializable;

/**
 * @author adriandlph / airondlph
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class BankDTO implements Serializable {
    private Long id;
    private String name;

    @Override
    public String toString() {
        return new StringBuilder("BankDTO{")
            .append("id=").append(id)
            .append(", name=").append(name)
            .append('}')
            .toString();
    }
}
