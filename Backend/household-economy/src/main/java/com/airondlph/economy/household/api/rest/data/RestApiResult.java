package com.airondlph.economy.household.api.rest.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author adriandlph / airondlph
 */
@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestApiResult<T> implements Serializable {
    private final int code;
    private final String message;
    private final T data;

    private RestApiResult() {
        this(1,  "Error.", null);
    }

    private RestApiResult(int code, String message, T data) {
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public static <T> RestApiResult<T> Create(int code, String message, T data) {
        return new RestApiResult<T>(code, message, data);
    }

    public static <T> RestApiResult<T> Ok(T data) {
        return Create(0, "Ok.", data);
    }

    public static <T> RestApiResult<T> Error(int code, String message) {
        return Create(code, message, null);
    }

    public static <T> RestApiResult<T> Error() {
        return Create(1, "Error.", null);
    }

    @Override
    public String toString() {
        return new StringBuilder("RestApiResult{")
            .append("code=").append(code)
            .append(", message=").append(message)
            .append(", data=").append(data)
            .append('}')
            .toString();
    }

}
