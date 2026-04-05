package com.amr.exchange.dashboard;

import com.amr.exchange.api.ExchangeService;
import com.amr.exchange.api.model.Analytics;
import com.amr.exchange.api.model.ExchangeRates;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class Dashboard implements Initializable {

    //current rate labels
    @FXML public Label usdToLbpRateLabel;
    @FXML public Label lbpToUsdRateLabel;

    //analytics labels
    @FXML public Label avgRateLabel;
    @FXML public Label minRateLabel;
    @FXML public Label maxRateLabel;
    @FXML public Label changeLabel;
    @FXML public Label volatilityLabel;
    @FXML public Label txCountLabel;
    @FXML public Label insightTrendLabel;
    @FXML public Label insightVolatilityLabel;
    @FXML public Label insightSpikeLabel;

    // controls
    @FXML public DatePicker startDatePicker;
    @FXML public DatePicker endDatePicker;
    @FXML public RadioButton usdToLbpRadio;
    @FXML public RadioButton lbpToUsdRadio;
    @FXML public Label statusLabel;

    private static final DateTimeFormatter PICKER_FMT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //default date range is the last 72 hours
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(3));
        usdToLbpRadio.setSelected(true);

        fetchRates();
        fetchAnalytics();
    }

    @FXML
    public void onRefresh() {
        statusLabel.setText("Refreshing...");
        fetchRates();
        fetchAnalytics();
    }

    private void fetchRates() {
        ExchangeService.exchangeApi().getExchangeRates()
                .enqueue(new Callback<ExchangeRates>() {
                    @Override
                    public void onResponse(Call<ExchangeRates> call,
                                           Response<ExchangeRates> response) {
                        Platform.runLater(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                ExchangeRates rates = response.body();
                                usdToLbpRateLabel.setText(
                                        rates.usdToLbpRate != null
                                                ? String.format("%.2f LBP", rates.usdToLbpRate)
                                                : "No data"
                                );
                                lbpToUsdRateLabel.setText(
                                        rates.lbpToUsdRate != null
                                                ? String.format("%.6f USD", rates.lbpToUsdRate)
                                                : "No data"
                                );
                                statusLabel.setText("Rates loaded successfully.");
                            } else {
                                usdToLbpRateLabel.setText("Unavailable");
                                lbpToUsdRateLabel.setText("Unavailable");
                                statusLabel.setText("Failed to load rates.");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ExchangeRates> call, Throwable t) {
                        Platform.runLater(() -> {
                            usdToLbpRateLabel.setText("Network error");
                            lbpToUsdRateLabel.setText("Network error");
                            statusLabel.setText("Network error: is the backend running?");
                        });
                    }
                });
    }

    private void fetchAnalytics() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null) {
            statusLabel.setText("Please select a valid date range.");
            return;
        }
        if (!start.isBefore(end)) {
            statusLabel.setText("Start date must be before end date.");
            return;
        }

        boolean usdToLbp = usdToLbpRadio.isSelected();
        String startStr = start.format(PICKER_FMT);
        String endStr = end.format(PICKER_FMT);

        ExchangeService.exchangeApi()
                .getAnalytics(usdToLbp, startStr, endStr)
                .enqueue(new Callback<Analytics>() {
                    @Override
                    public void onResponse(Call<Analytics> call,
                                           Response<Analytics> response) {
                        Platform.runLater(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                Analytics a = response.body();
                                avgRateLabel.setText(String.format("%.2f", a.averageRate));
                                minRateLabel.setText(String.format("%.2f", a.minRate));
                                maxRateLabel.setText(String.format("%.2f", a.maxRate));
                                changeLabel.setText(String.format("%.2f%%", a.percentageChange));
                                volatilityLabel.setText(String.format("%.2f%%", a.volatilityPercent));
                                txCountLabel.setText(String.valueOf(a.transactionCount));

                                //color the percentage change green/red
                                changeLabel.setStyle(a.percentageChange >= 0
                                        ? "-fx-text-fill: green;"
                                        : "-fx-text-fill: red;");

                                //insights
                                insightTrendLabel.setText("Trend: " + (a.percentageChange >= 0
                                        ? "Rate is increasing ▲"
                                        : "Rate is decreasing ▼"));
                                insightVolatilityLabel.setText("Volatility: " +
                                        (a.volatilityPercent < 2 ? "Low fluctuations"
                                                : a.volatilityPercent < 5 ? "Moderate fluctuations"
                                                : "High fluctuations"));
                                insightSpikeLabel.setText(String.format(
                                        "Biggest spike: %.2f (max) vs %.2f (avg)",
                                        a.maxRate, a.averageRate));

                                statusLabel.setText("Analytics loaded successfully.");
                            } else if (response.code() == 200) {
                                clearAnalytics("No transactions in selected range.");
                            } else {
                                clearAnalytics("Failed to load analytics.");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<Analytics> call, Throwable t) {
                        Platform.runLater(() ->
                                clearAnalytics("Network error: is the backend running?")
                        );
                    }
                });
    }

    private void clearAnalytics(String message) {
        avgRateLabel.setText("—");
        minRateLabel.setText("—");
        maxRateLabel.setText("—");
        changeLabel.setText("—");
        volatilityLabel.setText("—");
        txCountLabel.setText("—");
        insightTrendLabel.setText("Trend: No data");
        insightVolatilityLabel.setText("Volatility: No data");
        insightSpikeLabel.setText("Biggest spike: No data");
        statusLabel.setText(message);
    }
}