package com.airondlph.economy.household.api.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.airondlph.economy.household.api.rest.exception.SecurityException;

import static com.airondlph.economy.household.util.LogUtils.Enter;
import static com.airondlph.economy.household.util.LogUtils.Error;
import static com.airondlph.economy.household.util.LogUtils.Exit;

/**
 * @author adriandlph / airondlph
 */
@Slf4j
public class SecurityRESTController {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    public static String getBearerTokenHeader() throws SecurityException {
        Enter(log, "getBearerTokenHeader", "");
        try {
            String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(AUTHORIZATION_HEADER);
            return token == null ? null : token.replace("Bearer ", "");
        } catch (Exception ex) {
            Error(log, "Error getting user authorization token", null, ex.getMessage());
            log.error("{}",  ex.getStackTrace().toString());
            throw new SecurityException("Token not defined.");
        } finally {
            Exit(log, "getBearerTokenHeader");
        }
    }

}
