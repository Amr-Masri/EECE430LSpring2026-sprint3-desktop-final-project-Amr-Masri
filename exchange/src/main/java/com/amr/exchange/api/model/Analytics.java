package com.amr.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class Analytics {
    @SerializedName("average_rate")
    public Double averageRate;

    @SerializedName("min_rate")
    public Double minRate;

    @SerializedName("max_rate")
    public Double maxRate;

    @SerializedName("percentage_change")
    public Double percentageChange;

    @SerializedName("volatility_percent")
    public Double volatilityPercent;

    @SerializedName("transaction_count")
    public Integer transactionCount;

    @SerializedName("direction")
    public String direction;
}