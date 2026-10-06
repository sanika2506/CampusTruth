package com.campustruth.model;

public class User {
    private final String id;
    private final String name;
    private final UserRole role;
    private static int userCount = 0;

    public User(String name, UserRole role) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required");
        this.id = "U-" + (++userCount);
        this.name = name;
        this.role = role;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public UserRole getRole() { return role; }
    public boolean canVerify() { return role == UserRole.FACULTY_VERIFIER || role == UserRole.ADMIN; }
}
