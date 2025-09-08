package com.ucd.bookshop.controllers.dto;

import java.util.UUID;
import com.ucd.bookshop.constants.Role;
import com.ucd.bookshop.model.User;

public class UserDto {

    private UUID id;
    private String userName;
    private Role role;
    private Boolean isUsing2FA;
    private String secret;

    public UserDto(UUID id, String userName, Integer roleId, String secret, Boolean isUsing2FA) {
        this.id = id;
        this.userName = userName;
        this.role = Role.fromId(roleId);
        this.isUsing2FA = isUsing2FA;
        this.secret = secret;

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

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public Boolean getIsUsing2FA() { return isUsing2FA; }     // <- fixed naming
    public void setIsUsing2FA(Boolean isUsing2FA) { this.isUsing2FA = isUsing2FA; }

}
