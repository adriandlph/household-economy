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
public class TokenStrDTO implements Serializable {

    private String token;

}
