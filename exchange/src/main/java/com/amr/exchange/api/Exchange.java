package com.amr.exchange.api;

import com.amr.exchange.api.model.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Exchange {
    @POST("/user")
    Call<User> addUser(@Body User user);

    @POST("/authentication")
    Call<Token> authenticate(@Body User user);

    @GET("/exchangeRate")
    Call<ExchangeRates> getExchangeRates();

    @GET("/analytics")
    Call<Analytics> getAnalytics(
        @Query("usd_to_lbp") boolean usdToLbp,
        @Query("start_date") String startDate,
        @Query("end_date") String endDate
    );

    @GET("/exchangeRateHistory")
    Call<RateHistory> getExchangeRateHistory(
            @Query("usd_to_lbp") boolean usdToLbp,
            @Query("interval") String interval,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );
}