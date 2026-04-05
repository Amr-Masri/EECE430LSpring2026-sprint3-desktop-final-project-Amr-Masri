package com.amr.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class ExchangeRates {
    @SerializedName("usd_to_lbp_rate")
    public Double usdToLbpRate;

    @SerializedName("lbp_to_usd_rate")
    public Double lbpToUsdRate;
}