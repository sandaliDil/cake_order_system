package com.example.foodordersystem;

import com.example.foodordersystem.model.User;

public class Session {
    private static Session instance;
    private User loggedInUser;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

}
