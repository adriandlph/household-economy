package com.airondlph.economy.household.api.rest;

import com.airondlph.economy.household.api.rest.data.RestApiResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author adriandlph / airondlph
 */
@ControllerAdvice
@Slf4j
public class Handler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestApiResult<Void>> handle(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        // TODO: check bad request for error in json or not founds when uri not found
        log.error("{}\n{}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(-1, "Server error."));
    }

}
