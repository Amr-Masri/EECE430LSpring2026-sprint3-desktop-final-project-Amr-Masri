package com.amr.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class RateHistoryPoint {
    @SerializedName("timestamp")
    public String timestamp;

    @SerializedName("average_rate")
    public Double averageRate;

    @SerializedName("transaction_count")
    public Integer transactionCount;
}