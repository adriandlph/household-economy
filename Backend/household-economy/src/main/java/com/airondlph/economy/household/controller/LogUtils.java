package com.airondlph.economy.household.controller;

import org.slf4j.Logger;

public class LogUtils {

    public static void Enter(Logger log, String methodName) {
        log.info("---> {}", methodName);
    }

    public static void Exit(Logger log, String methodName) {
        log.info("<--- {}", methodName);
    }

    public static void ErrorWarning(Logger log, String intro, Integer errCode, String errorMsg) {
        log.warn("{}\n\t-Code: {}\n\t-Message: {}", intro, errCode, errorMsg);
    }
    public static void Error(Logger log, String intro, Integer errCode, String errorMsg) {
        log.error("{}\n\t-Code: {}\n\t-Message: {}", intro, errCode, errorMsg);
    }

}
