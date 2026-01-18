package com.airondlph.economy.household.api.rest.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author adriandlph / airondlph
 */
public class SecurityException extends Exception {
    @Getter @Setter
    private int code;

    public SecurityException(String message) {
        this(0, message);
    }

    public SecurityException(int code, String message) {
        this.code = code;
        super(message);
    }
}
