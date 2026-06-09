package com.npopov.philharmonic.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserModel {
    private Long id;
    private String username;
    private String password;
    private String email;
    private Set<RoleModel> roles;
    private Boolean enabled = true;

    public UserModel(Long id, String username, String password, String email, Set<RoleModel> roles, Boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
        this.enabled = enabled;
    }

    public UserModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<RoleModel> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleModel> roles) {
        this.roles = roles;
    }

    public Boolean getPermission(CrudPermissions permission) {
        for (RoleModel role : this.roles) {
            if (role.getPermission(permission)) return true;
        }
        return false;
    }

    public Boolean getEnabled() {return enabled;}

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
