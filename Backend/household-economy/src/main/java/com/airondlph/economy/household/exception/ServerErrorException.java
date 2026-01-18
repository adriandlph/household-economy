package com.airondlph.economy.household.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author adriandlph / airondlph
 */
public class ServerErrorException extends Exception {
    @Getter @Setter
    private int code;

    public ServerErrorException(String message) {
        this(0, message);
    }

    public ServerErrorException(int code, String message) {
        this.code = code;
        super(message);
    }

    public ServerErrorException(int code, String message, Exception ex) {
        this.code = code;
        super(message, ex);
    }
}
