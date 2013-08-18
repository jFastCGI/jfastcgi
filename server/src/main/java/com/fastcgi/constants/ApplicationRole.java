package com.fastcgi.constants;

public enum ApplicationRole {
    RESPONDER(1, "RESPONDER"),
    AUTHORIZER(2, "AUTHORIZER"),
    FILTER(3, "FILTER");

    private final int value;
    private final String roleName;

    private ApplicationRole(int value, final String roleName){
        this.value = value;
        this.roleName = roleName;
    }

    public int getValue() {
        return value;
    }

    public static ApplicationRole getByValue(final int role) {
        if(role < 1 || role > 3){
            throw new IllegalArgumentException("Role has invalid value (must be between 1 and 3): " + role);
        }
        return values()[role-1];
    }

    public String getRoleName() {
        return roleName;
    }
}
