package com.airondlph.economy.household.data.model;

import lombok.*;
import com.airondlph.economy.household.data.VO;

/**
 * @author adriandlph / airondlph
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserVO implements VO {

    private Long id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean emailValidated;
    private UserVO parentUser;

    @Override
    public String toString() {
        return new StringBuilder("UserVO{")
            .append("id=").append(id)
            .append(", username=").append(username)
            .append(", firstName=").append(firstName)
            .append(", lastName=").append(lastName)
            .append(", email=").append(email)
            .append(", emailValidated=").append(emailValidated)
            .append(", parentUser=").append(parentUser)
            .append('}')
            .toString();
    }

}
