package com.ucd.bookshop.controllers.dto;

import java.util.UUID;

import com.ucd.bookshop.constants.Role;

public class UserDto {

    private UUID id;
    private String userName;
    private Role role;

    public UserDto(UUID id, String userName, Integer roleId) {
        this.id = id;
        this.userName = userName;
        this.role = Role.fromId(roleId);
    }

    public UUID getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public Role getRole() {
        return role;
    }
}
