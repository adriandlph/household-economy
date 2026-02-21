package com.airondlph.economy.household.util;

import lombok.Getter;

/**
 * @author adriandlph / airondlph
 */
@Getter
public class ValidationResult {
    private final boolean valid;
    private final int errCode;
    private final String errMsg;


    private ValidationResult(boolean valid, int errCode, String errMsg) {
        this.valid = valid;
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, 0, "");
    }

    public static ValidationResult error(int errCode) {
        return new ValidationResult(false, errCode, "");
    }

    public static ValidationResult error(int errCode, String errMsg) {
        return new ValidationResult(false, errCode, errMsg);
    }

    @Override
    public String toString() {
        if (isValid()) return "ValidationResult{ valid=true }";

        return new StringBuilder("ValidationResult{")
                .append("valid=").append(valid)
                .append(", errCode=").append(errCode)
                .append(", errMsg=").append(errMsg)
                .append('}')
                .toString();
    }

}

