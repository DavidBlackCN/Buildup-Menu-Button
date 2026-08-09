package com.davidblackcn.buildupmenubutton.client.profile;

public record RoleAssignment(ButtonRole role, int coreOrder) {
    public static RoleAssignment ignored() {
        return new RoleAssignment(ButtonRole.IGNORED, Integer.MAX_VALUE);
    }
}
