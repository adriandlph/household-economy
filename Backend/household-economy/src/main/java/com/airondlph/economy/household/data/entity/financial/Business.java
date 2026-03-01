package com.airondlph.economy.household.data.entity.financial;

import com.airondlph.economy.household.data.HasVO;
import com.airondlph.economy.household.data.model.BusinessVO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * @author adriandlph / airondlph
 */
@Entity(name = "Business")
@Table(name = "business")
@Inheritance(strategy = InheritanceType.JOINED)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Business implements HasVO, Serializable {

    public static final int NAME_MAX_LENGTH = 255;

    @Column(name = "id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;
    @Column(name = "name", length = NAME_MAX_LENGTH, nullable = false)
    @Getter @Setter
    private String name;

    @Override
    public BusinessVO getVO() {
        return BusinessVO.builder()
            .id(getId())
            .name(getName())
            .build();
    }

    @Override
    public String toString() {
        return new StringBuilder("Business{")
            .append("id=").append(getId())
            .append(", name=").append(getName())
            .append('}')
            .toString();
    }

}
