package com.airondlph.economy.household.data.entity.user;

import com.airondlph.economy.household.data.enumeration.UserValidationType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Calendar;

/**
 * @author adriandlph / airondlph
 */
@Entity(name = "UserValidation")
@Table(name = "user_validation")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserValidation implements Serializable {
    public final static int CODE_MAX_LENGTH = 256;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;
    @Column(name = "code", length = CODE_MAX_LENGTH, nullable = false)
    @Getter @Setter
    private String code;
    @Column(name = "expires")
    @Getter @Setter
    private Calendar expires;
    @Column(name = "type")
    @Enumerated(EnumType.ORDINAL)
    @Getter @Setter
    private UserValidationType type;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne
    @Getter @Setter
    private User user;

}
