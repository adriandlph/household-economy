package com.airondlph.economy.household.data.model;

import com.airondlph.economy.household.data.VO;
import com.airondlph.economy.household.data.enumeration.TokenType;
import lombok.*;

import java.util.Calendar;

/**
 * @author adriandlph / airondlph
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TokenVO implements VO {

    private Long id;
    private String token;
    private Calendar expires;
    private TokenType type;
    private UserVO userVO;

    @Override
    public String toString() {
        return new StringBuilder("TokenVO{")
            .append("id=").append(id)
            .append(", token=").append(token)
            .append(", expires=").append(expires)
            .append(", type=").append(type.name())
            .append(", userVO=").append(userVO.getId())
            .append('}')
            .toString();
    }

}
