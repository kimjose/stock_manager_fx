package utils;

import models.auth.User;

public class SessionManager {
    public User user;
    public static SessionManager INSTANCE;

    private SessionManager(User user){
        this.user = user;
    }

    public static SessionManager getInstance(User user) {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager(user);
        }else{
            INSTANCE.user = user;
        }
        return INSTANCE;
    }
}
