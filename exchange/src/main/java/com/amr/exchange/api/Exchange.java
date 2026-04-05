package com.amr.exchange.api;

import com.amr.exchange.api.model.Token;
import com.amr.exchange.api.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Exchange {
    @POST("/user")
    Call<User> addUser(@Body User user);

    @POST("/authentication")
    Call<Token> authenticate(@Body User user);
}