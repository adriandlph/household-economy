package com.airondlph.economy.household.data.entity.user;

import com.airondlph.economy.household.data.HasVO;
import com.airondlph.economy.household.data.enumeration.TokenType;
import com.airondlph.economy.household.data.model.TokenVO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Calendar;

/**
 * @author adriandlph / airondlph
 */
@Entity(name = "Token")
@Table(name = "token")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token implements Serializable, HasVO {
    public static final int TOKEN_MAX_LENGTH = 2048;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;
    @Column(name = "token", length = TOKEN_MAX_LENGTH, nullable = false)
    @Getter @Setter
    private String token;
    @Column(name = "expires")
    @Getter @Setter
    private Calendar expires;
    @Column(name = "type")
    @Enumerated(EnumType.ORDINAL)
    @Getter @Setter
    private TokenType type;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne
    @Getter @Setter
    private User user;

    @Override
    public TokenVO getVO() {
        return TokenVO.builder()
            .id(id)
            .token(token)
            .expires(expires)
            .type(type)
            .build();
    }

    @Override
    public String toString() {
        return new StringBuilder("Token{")
            .append("id=").append(id)
            .append(", token=").append(token)
            .append(", expires=").append(expires)
            .append(", type=").append(type)
            .append('}')
            .toString();
    }

}
