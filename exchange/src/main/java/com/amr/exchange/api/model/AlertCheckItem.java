package com.amr.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class AlertCheckItem {
    @SerializedName("id")
    public Integer id;

    @SerializedName("usd_to_lbp")
    public Boolean usdToLbp;

    @SerializedName("threshold")
    public Double threshold;

    @SerializedName("direction")
    public String direction;

    @SerializedName("current_rate")
    public Double currentRate;

    @SerializedName("creation_date")
    public String creationDate;
}