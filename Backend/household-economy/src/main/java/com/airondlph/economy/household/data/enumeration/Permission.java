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

    ;


    private PermissionGroup group;
    private Permission(PermissionGroup group) {
        this.group = group;
    }

    public PermissionGroup getGroup() {
        return group;
    }
}
