package com.npopov.philharmonic.client.model;

import java.util.HashMap;
import java.util.Map;

public class RoleModel {
    String name;
    String description;
    Map<CrudPermissions, Boolean> permissions;

    public RoleModel(String name, String description, Map<CrudPermissions, Boolean> permissions) {
        this.name = name;
        this.description = description;
        this.permissions = permissions;
    }

    public RoleModel() {
        this.name = "";
        this.description = "";
        this.permissions = new HashMap<>();
        this.permissions.put(CrudPermissions.CREATE, false);
        this.permissions.put(CrudPermissions.READ, false);
        this.permissions.put(CrudPermissions.DELETE, false);
        this.permissions.put(CrudPermissions.UPDATE, false);
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

    public Map<CrudPermissions, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<CrudPermissions, Boolean> permissions) {
        this.permissions = permissions;
    }

    public Boolean getPermission(CrudPermissions permission) {
        return permissions.get(permission);
    }

    public void setPermission(CrudPermissions permission, boolean value) {
        permissions.put(permission, value);
    }
}
