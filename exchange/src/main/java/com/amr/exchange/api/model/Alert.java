package com.amr.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class Alert {
    @SerializedName("id")
    public Integer id;

    @SerializedName("user_id")
    public Integer userId;

    @SerializedName("usd_to_lbp")
    public Boolean usdToLbp;

    @SerializedName("threshold")
    public Double threshold;

    @SerializedName("direction")
    public String direction;

    @SerializedName("creation_date")
    public String creationDate;


    public Alert(Boolean usdToLbp, Double threshold, String direction) {
        this.usdToLbp = usdToLbp;
        this.threshold = threshold;
        this.direction = direction;
    }


    public Integer getId()          { return id; }
    public Integer getUserId()      { return userId; }
    public Boolean getUsdToLbp()    { return usdToLbp; }
    public Double getThreshold()    { return threshold; }
    public String getDirection()    { return direction; }
    public String getCreationDate() { return creationDate; }
}