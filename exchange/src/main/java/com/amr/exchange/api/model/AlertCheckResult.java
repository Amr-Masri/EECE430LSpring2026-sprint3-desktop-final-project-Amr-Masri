package com.amr.exchange.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AlertCheckResult {
    @SerializedName("triggered_alerts")
    public List<AlertCheckItem> triggeredAlerts;

    @SerializedName("untriggered_alerts")
    public List<AlertCheckItem> untriggeredAlerts;

    @SerializedName("current_usd_to_lbp_rate")
    public Double currentUsdToLbpRate;

    @SerializedName("current_lbp_to_usd_rate")
    public Double currentLbpToUsdRate;
}