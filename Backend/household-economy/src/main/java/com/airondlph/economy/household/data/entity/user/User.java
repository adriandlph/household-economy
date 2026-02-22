package com.airondlph.economy.household.data.entity.user;


import com.airondlph.economy.household.data.HasVO;
import com.airondlph.economy.household.data.model.UserVO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * @author adriandlph / airondlph
 */
@Entity(name = "User")
@Table(name = "user")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements HasVO, Serializable {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;
    @Column(name = "username", length = 50, nullable = false)
    @Getter @Setter
    private String username;
    @Column(name = "password", length = 150, nullable = false)
    @Getter @Setter
    private String password;
    @Column(name = "first_name", length = 100, nullable = false)
    @Getter @Setter
    private String firstName;
    @Column(name = "last_name", length = 300)
    @Getter @Setter
    private String lastName;
    @Column(name = "email", length = 250, nullable = false)
    @Getter @Setter
    private String email;
    @Column(name = "email_validated", nullable = false)
    @Getter @Setter
    @Builder.Default // Avoids builder to set null this argument
    private Boolean emailValidated = false;
    @JoinColumn(name = "parent_user_id", nullable = true, referencedColumnName = "id")
    @ManyToOne
    @Getter @Setter
    private User parentUser;

    @Override
    public UserVO getVO() {
        return UserVO.builder()
            .id(id)
            .username(username)
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .emailValidated(emailValidated)
            .parentUser(null)
            .build();
    }

    @Override
    public String toString() {
        return new StringBuilder("User{")
            .append("id=").append(id)
            .append(", username=").append(username)
            .append(", firstName=").append(firstName)
            .append(", lastName=").append(lastName)
            .append(", email=").append(email)
            .append(", emailValidated=").append(emailValidated)
            .append(", parentUser=").append(simpleToString())
            .append('}')
            .toString();
    }

    public String simpleToString() {
        return new StringBuilder("User{")
            .append("id=").append(id)
            .append(", username=").append(username)
            .append('}')
            .toString();
    }

}
