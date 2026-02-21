package com.airondlph.economy.household.data.enumeration;

/**
 * @author adriandlph / airondlph
 */
public enum Permission {

    SYSTEM(PermissionGroup.GOD),
    ADMIN(PermissionGroup.GOD),

    ADD_USER(PermissionGroup.USERS_PERMISSIONS),
    ADD_ALL_USER(PermissionGroup.USERS_PERMISSIONS),
    GET_USER(PermissionGroup.USERS_PERMISSIONS),
    GET_ALL_USER(PermissionGroup.USERS_PERMISSIONS),
    DELETE_USER(PermissionGroup.USERS_PERMISSIONS),
    DELETE_ALL_USER(PermissionGroup.USERS_PERMISSIONS),
    EDIT_USER(PermissionGroup.USERS_PERMISSIONS),
    EDIT_ALL_USER(PermissionGroup.USERS_PERMISSIONS),

    SEND_USER_EMAIL_VALIDATION_CODE(PermissionGroup.USERS_PERMISSIONS),
    SEND_ALL_USER_EMAIL_VALIDATION_CODE(PermissionGroup.USERS_PERMISSIONS),

    ADD_BANK(PermissionGroup.SYSTEM_FINANCIAL_PERMISSIONS),
    GET_BANK(PermissionGroup.SYSTEM_FINANCIAL_PERMISSIONS),
    DELETE_BANK(PermissionGroup.SYSTEM_FINANCIAL_PERMISSIONS),
    EDIT_BANK(PermissionGroup.SYSTEM_FINANCIAL_PERMISSIONS),

    ADD_INCOME_OPERATION(PermissionGroup.USER_FINANCIAL_PERMISSIONS),
    GET_INCOME_OPERATION(PermissionGroup.USER_FINANCIAL_PERMISSIONS),
    DELETE_INCOME_OPERATION(PermissionGroup.USER_FINANCIAL_PERMISSIONS),
    EDIT_INCOME_OPERATION(PermissionGroup.USER_FINANCIAL_PERMISSIONS),

    ADD_OUTCOME_OPERATION(PermissionGroup.USER_FINANCIAL_PERMISSIONS),
    GET_OUTCOME_OPERATION(PermissionGroup.USER_FINANCIAL_PERMISSIONS),
    DELETE_OUTCOME_OPERATION(PermissionGroup.USER_FINANCIAL_PERMISSIONS),
    EDIT_OUTCOME_OPERATION(PermissionGroup.USER_FINANCIAL_PERMISSIONS),

    ;


    private PermissionGroup group;
    private Permission(PermissionGroup group) {
        this.group = group;
    }

    public PermissionGroup getGroup() {
        return group;
    }
}
