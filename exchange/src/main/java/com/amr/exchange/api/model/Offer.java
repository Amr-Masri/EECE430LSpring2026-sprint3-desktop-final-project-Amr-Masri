package com.amr.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class Offer {
    @SerializedName("id")
    public Integer id;

    @SerializedName("user_id")
    public Integer userId;

    @SerializedName("usd_amount")
    public Double usdAmount;

    @SerializedName("lbp_amount")
    public Double lbpAmount;

    @SerializedName("usd_to_lbp")
    public Boolean usdToLbp;

    @SerializedName("status")
    public String status;

    @SerializedName("creation_date")
    public String creationDate;

    @SerializedName("accepted_by")
    public Integer acceptedBy;

    @SerializedName("accepted_at")
    public String acceptedAt;

    //constructor for new offers
    public Offer(Double usdAmount, Double lbpAmount, Boolean usdToLbp) {
        this.usdAmount = usdAmount;
        this.lbpAmount = lbpAmount;
        this.usdToLbp = usdToLbp;
    }

    //getters for TableView
    public Integer getId()         { return id; }
    public Integer getUserId()     { return userId; }
    public Double getUsdAmount()   { return usdAmount; }
    public Double getLbpAmount()   { return lbpAmount; }
    public Boolean getUsdToLbp()   { return usdToLbp; }
    public String getStatus()      { return status; }
    public String getCreationDate(){ return creationDate; }
    public Integer getAcceptedBy() { return acceptedBy; }
    public String getAcceptedAt()  { return acceptedAt; }
}