package com.example.share.models;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    private String name, image, email, password, token, id;

    public User() {
    }

    public User(String name, String image, String email, String password, String token, String id) {
        this.name = name;
        this.image = image;
        this.email = email;
        this.password = password;
        this.token = token;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}