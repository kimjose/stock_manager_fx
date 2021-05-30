package utils;

import models.auth.User;

public enum SessionManager {
    INSTANCE;
    User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
