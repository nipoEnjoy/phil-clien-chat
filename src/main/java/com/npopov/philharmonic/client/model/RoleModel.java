package com.npopov.philharmonic.client.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoleModel {
    Long id;
    String name;
    String description;
    Set<CrudPermissions> permissions;

    public RoleModel(Long id, String name, String description, Set<CrudPermissions> permissions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissions = permissions != null ? permissions : new HashSet<>();
    }

    public RoleModel() {
        this.permissions = new HashSet<>();
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }

    public Set<CrudPermissions> getPermissions() { return permissions; }
    public void setPermissions(Set<CrudPermissions> permissions) { this.permissions = permissions; }

    public boolean hasPermission(CrudPermissions permission) {
        return permissions != null && permissions.contains(permission);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getPermission(CrudPermissions permission) {
        return permissions.add(permission);
    }

    public void setPermission(CrudPermissions permission) {
        permissions.add(permission);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
