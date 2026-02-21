package com.airondlph.economy.household.data.model;

import com.airondlph.economy.household.data.VO;
import lombok.*;

/**
 * @author adriandlph / airondlph
 */
@Builder
@Getter @Setter
public class BusinessVO implements VO {

    private Long id;
    private String name;

    protected BusinessVO() {

    }

    protected BusinessVO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return new StringBuilder("BusinessVO{")
            .append("id=").append(id)
            .append(", name=").append(name)
            .append('}')
            .toString();
    }

}
