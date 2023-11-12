package edu.northeastern.group_project_group_8;

import java.util.UUID;

public class User {
    String userId;
    String username;

    User(String username) {
        this.username = username;
        this.userId = UUID.randomUUID().toString();
    }

    User(String username, String userId) {
        this.username = username;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }
}
