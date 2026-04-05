package com.amr.exchange.api;

import com.amr.exchange.api.model.*;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

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

    @POST("/transaction")
    Call<Object> addTransaction(
        @Body Transaction transaction,
        @Header("Authorization") String authorization
    );

    @GET("/transaction")
    Call<List<Transaction>> getUserTransactions(
        @Header("Authorization") String authorization
    );

    @GET("/export")
    Call<ResponseBody> exportTransactions(
            @Header("Authorization") String authorization
    );

    @GET("/market/offers")
    Call<List<Offer>> getAvailableOffers();

    @POST("/market/offers")
    Call<Offer> createOffer(
            @Body Offer offer,
            @Header("Authorization") String authorization
    );

    @POST("/market/offers/{id}/accept")
    Call<Offer> acceptOffer(
            @Path("id") int offerId,
            @Header("Authorization") String authorization
    );

    @DELETE("/market/offers/{id}")
    Call<ResponseBody> cancelOffer(
            @Path("id") int offerId,
            @Header("Authorization") String authorization
    );

    @GET("/market/trades")
    Call<List<Offer>> getMyTrades(
            @Header("Authorization") String authorization
    );
}