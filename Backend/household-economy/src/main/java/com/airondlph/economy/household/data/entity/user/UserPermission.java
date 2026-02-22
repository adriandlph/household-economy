package com.airondlph.economy.household.data.entity.user;

import com.airondlph.economy.household.data.enumeration.Permission;
import jakarta.persistence.*;
import lombok.*;

/**
 * @author adriandlph / airondlph
 */
@Entity(name = "UserPermission")
@Table(name = "user_permission")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermission {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;
    @Column(name = "permission", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @Getter @Setter
    private Permission permission;
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    @ManyToOne
    @Getter @Setter
    private User user;

    @Override
    public String toString() {
        return new StringBuilder("UserPermission{")
            .append("id=").append(id)
            .append(", permission=").append(permission.name())
            .append(", user=").append(user.simpleToString())
            .append('}')
            .toString();
    }

}
