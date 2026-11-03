package ar.edu.itba.paw.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

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

    public static Optional<Role> fromString(final String roleName){
        return Arrays.stream(Role.values())
                .filter(r -> r.roleName.equalsIgnoreCase(roleName) || r.name().equalsIgnoreCase(roleName))
                .findFirst();
    }
}
