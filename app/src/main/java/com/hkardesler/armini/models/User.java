/*
 * *
 *  * Created by Haydar Kardesler on 21.05.2022 01:07
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 21.05.2022 01:05
 *
 */

package com.hkardesler.armini.models;

public class User {
    private String userId, fullName, email, password;

    public User() {

    }

    public User(String userId, String fullName, String email, String password) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
