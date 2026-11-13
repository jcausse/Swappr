package ar.edu.itba.paw.model;

import lombok.Getter;

public enum Role {
    USER("USER"),
    ADMIN("ADMIN");

    @Getter private final String roleName;
    @Getter private final String springRoleName;

    private static final String SPRING_PREFIX = "ROLE_";

    Role(String roleName) {
        this.roleName = roleName;
        this.springRoleName = SPRING_PREFIX + roleName;
    }
}
