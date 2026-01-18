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
public class UserDTO implements Serializable {
    private Long id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean emailValidated;

    @Override
    public String toString() {
        return new StringBuilder("CreateUserData{")
            .append("username=").append(username)
            .append(", firstName=").append(firstName)
            .append(", lastName=").append(lastName)
            .append(", email=").append(email)
            .append(", emailValidated=").append(emailValidated)
            .append('}')
            .toString();
    }
}
