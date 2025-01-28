package main.model.enums;

import lombok.Getter;

@Getter
public enum Permission {
    USER("user:write"),
    MODERATE("user:moderator");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

}
