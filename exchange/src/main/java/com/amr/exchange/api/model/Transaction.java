package com.amr.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("id")
    public Integer id;

    @SerializedName("usd_amount")
    public Double usdAmount;

    @SerializedName("lbp_amount")
    public Double lbpAmount;

    @SerializedName("usd_to_lbp")
    public Boolean usdToLbp;

    @SerializedName("added_date")
    public String addedDate;

    @SerializedName("user_id")
    public Integer userId;

    @SerializedName("source")
    public String source;

    @SerializedName("is_outlier")
    public Boolean isOutlier;

    //constructor used when posting a new transaction
    public Transaction(Double usdAmount, Double lbpAmount, Boolean usdToLbp) {
        this.usdAmount = usdAmount;
        this.lbpAmount = lbpAmount;
        this.usdToLbp = usdToLbp;
    }

    //getters whih are needed for TableView Propertyvaluefactory
    public Integer getId()       { return id; }
    public Double getUsdAmount() { return usdAmount; }
    public Double getLbpAmount() { return lbpAmount; }
    public Boolean getUsdToLbp() { return usdToLbp; }
    public String getAddedDate() { return addedDate; }
    public String getSource()    { return source; }
    public Boolean getIsOutlier(){ return isOutlier; }
}