package com.amr.exchange.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RateHistory {
    @SerializedName("direction")
    public String direction;

    @SerializedName("interval")
    public String interval;

    @SerializedName("start_date")
    public String startDate;

    @SerializedName("end_date")
    public String endDate;

    @SerializedName("data")
    public List<RateHistoryPoint> data;
}