package com.amr.exchange.graph;

import com.amr.exchange.api.ExchangeService;
import com.amr.exchange.api.model.RateHistory;
import com.amr.exchange.api.model.RateHistoryPoint;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class Graph implements Initializable {

    @FXML public LineChart<String, Number> rateChart;
    @FXML public DatePicker startDatePicker;
    @FXML public DatePicker endDatePicker;
    @FXML public RadioButton usdToLbpRadio;
    @FXML public RadioButton lbpToUsdRadio;
    @FXML public RadioButton dailyRadio;
    @FXML public RadioButton hourlyRadio;
    @FXML public Label statusLabel;
    @FXML public Label summaryLabel;

    private static final DateTimeFormatter PICKER_FMT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // defaults: last 7 days, daily, usd→lbp
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(7));
        usdToLbpRadio.setSelected(true);
        dailyRadio.setSelected(true);

        // chart look
        rateChart.setAnimated(false);
        rateChart.setCreateSymbols(true);

        fetchHistory();
    }

    @FXML
    public void onApply() {
        fetchHistory();
    }

    private void fetchHistory() {
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
        String interval = dailyRadio.isSelected() ? "daily" : "hourly";
        String startStr = start.format(PICKER_FMT);
        String endStr = end.format(PICKER_FMT);

        statusLabel.setText("Loading chart data...");
        summaryLabel.setText("");

        ExchangeService.exchangeApi()
                .getExchangeRateHistory(usdToLbp, interval, startStr, endStr)
                .enqueue(new Callback<RateHistory>() {
                    @Override
                    public void onResponse(Call<RateHistory> call,
                                           Response<RateHistory> response) {
                        Platform.runLater(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                RateHistory history = response.body();

                                if (history.data == null || history.data.isEmpty()) {
                                    clearChart("No data for selected range.");
                                    return;
                                }

                                buildChart(history, usdToLbp, interval);
                                statusLabel.setText("Chart loaded successfully.");
                            } else {
                                clearChart("Failed to load chart data.");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<RateHistory> call, Throwable t) {
                        Platform.runLater(() ->
                                clearChart("Network error: ensure that backend is running")
                        );
                    }
                });
    }

    private void buildChart(RateHistory history, boolean usdToLbp, String interval) {
        rateChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName((usdToLbp ? "USD → LBP" : "LBP → USD")
                + " (" + interval + ")");

        double minRate = Double.MAX_VALUE;
        double maxRate = Double.MIN_VALUE;
        double sumRate = 0;
        int count = 0;

        for (RateHistoryPoint point : history.data) {
            // shorten the timestamp label for readability
            String label = shortenTimestamp(point.timestamp, interval);
            series.getData().add(
                    new XYChart.Data<>(label, point.averageRate)
            );

            if (point.averageRate < minRate) minRate = point.averageRate;
            if (point.averageRate > maxRate) maxRate = point.averageRate;
            sumRate += point.averageRate;
            count++;
        }

        rateChart.getData().add(series);

        // summary below the chart
        double avg = sumRate / count;
        summaryLabel.setText(String.format(
                "Points: %d  |  Min: %.2f  |  Max: %.2f  |  Avg: %.2f",
                count, minRate, maxRate, avg
        ));
    }

    private String shortenTimestamp(String timestamp, String interval) {
        // timestamp from backend looks like "2026-02-22T00:00:00"
        // for daily: show "02-22", for hourly: show "02-22 14:00"
        try {
            if (interval.equals("daily")) {
                // take just MM-DD
                return timestamp.substring(5, 10);
            } else {
                // take MM-DD HH:mm
                return timestamp.substring(5, 10)
                        + " " + timestamp.substring(11, 16);
            }
        } catch (Exception e) {
            return timestamp;
        }
    }

    private void clearChart(String message) {
        rateChart.getData().clear();
        statusLabel.setText(message);
        summaryLabel.setText("");
    }
}