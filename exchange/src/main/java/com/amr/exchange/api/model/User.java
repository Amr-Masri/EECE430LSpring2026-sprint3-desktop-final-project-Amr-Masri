package com.amr.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    public Integer id;

    @SerializedName("user_name")
    public String userName;

    @SerializedName("password")
    public String password;

    @SerializedName("role")
    public String role;

    @SerializedName("status")
    public String status;

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
}