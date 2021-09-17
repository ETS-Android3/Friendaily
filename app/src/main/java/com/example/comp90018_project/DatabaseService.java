package com.example.comp90018_project;

public class DatabaseService {

    public DatabaseService() {

    }

    public boolean login(String account, String password) {
        // TODO: 2021/9/17
        if (account.length() == 0 || password.length() == 0) {
            return false;
        }
        // first find the specific account which stored in database (can login with email or username)
        // then check the password
        return true;
    }

    public boolean register(User user) {
        // TODO: 2021/9/17
        // store the information of user into the database
        if (user.getEmail().length() == 0 || user.getUsername().length() == 0 || user.getPassword().length() == 0) {
            return false;
        }
        return true;
    }

}
