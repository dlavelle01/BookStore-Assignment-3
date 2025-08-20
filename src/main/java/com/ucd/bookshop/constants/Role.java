package com.ucd.bookshop.constants;

/**
 * Role enum for system roles - no database table needed
 */
public enum Role {

    ADMIN(1, "ADMIN"),
    CUSTOMER(2, "CUSTOMER");

    private final int id;
    private final String name;

    Role(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Get role by ID
     */
    public static Role fromId(int id) {
        for (Role role : values()) {
            if (role.id == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role ID: " + id);
    }

    /**
     * Get role by name (case-insensitive)
     */
    public static Role fromName(String name) {
        for (Role role : values()) {
            if (role.name.equalsIgnoreCase(name)) {
                return role;
            }
        }
        
        throw new IllegalArgumentException("Invalid role name: " + name);
    }

    /**
     * Check if role ID is valid
     */
    public static boolean isValidId(int id) {
        try {
            fromId(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get role name by ID (returns "UNKNOWN" for invalid IDs)
     */
    public static String getNameById(int id) {
        try {
            return fromId(id).getName();
        } catch (IllegalArgumentException e) {
            return "UNKNOWN";
        }
    }

    @Override
    public String toString() {
        return name;
    }
}