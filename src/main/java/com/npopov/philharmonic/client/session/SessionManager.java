package com.npopov.philharmonic.client.session;

import java.util.Set;

/**
 * Singleton that holds the active session after login.
 * Stores the JWT token and decoded user roles for role-based UI.
 */
public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private String token;
    private String username;
    private Set<String> roles;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void login(String token, String username, Set<String> roles) {
        this.token    = token;
        this.username = username;
        this.roles    = Set.copyOf(roles);
    }

    public void logout() {
        this.token    = null;
        this.username = null;
        this.roles    = null;
    }

    public boolean isLoggedIn() {
        return token != null;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getRoles() {
        return roles != null ? roles : Set.of();
    }

    /** True if user has ADMIN or SUPERADMIN role — may create/edit records. */
    public boolean isAdmin() {
        return roles != null &&
               (roles.contains("ADMIN") || roles.contains("SUPERADMIN"));
    }

    /** True only for SUPERADMIN — may delete records. */
    public boolean isSuperAdmin() {
        return roles != null && roles.contains("SUPERADMIN");
    }

    /** Authorization header value, ready to pass in HTTP requests. */
    public String bearerHeader() {
        return "Bearer " + token;
    }
}
