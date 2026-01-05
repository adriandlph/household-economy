package com.airondlph.economy.household.data.enumeration;

/**
 * @author adriandlph / airondlph
 */
public enum EntityOperationEnum {
    GET,
    CREATE,
    UPDATE,
    DELETE,
    ;

    @Override
    public String toString() {
        return new StringBuilder(EntityOperationEnum.class.getName()).append("::").append(this.name()).toString();
    }
}
