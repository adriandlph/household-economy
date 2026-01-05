package com.airondlph.economy.household.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author adriandlph / airondlph
 */
public class ValidationException extends Exception {
    @Getter @Setter
    private int code;

    public ValidationException(String message) {
        this(0, message);
    }

    public ValidationException(int code, String message) {
        this.code = code;
        super(message);
    }
}
