package com.airondlph.economy.household.logic.data;

import lombok.Getter;

/**
 * @author adriandlph / airondlph
 */
@Getter
public class Result<T> {
    private final boolean valid;
    private final T result;
    private final int errCode;

    private Result() {
        this(false, 1, null);
    }

    private Result(boolean valid, int errCode, T result) {
        this.valid = valid;
        this.result = result;
        this.errCode = errCode;
    }

    public static <T> Result<T> create(T result) {
        return new Result<T>(true, 0, result);
    }

    public static <T> Result<T> create(int errCode) {
        return new Result<T>(false, errCode, null);
    }

    public static <T> Result<T> create(boolean valid, int errCode, T result) {
        return new Result<T>(valid, errCode, result);
    }

    @Override
    public String toString() {
        return new StringBuilder("Result{")
            .append("valid=").append(valid)
            .append(", result=").append(result)
            .append(", errCode=").append(errCode)
            .append('}')
            .toString();
    }
}
