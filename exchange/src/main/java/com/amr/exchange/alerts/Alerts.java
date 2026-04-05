package com.amr.exchange.alerts;

import com.amr.exchange.Authentication;
import com.amr.exchange.api.ExchangeService;
import com.amr.exchange.api.model.Alert;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.amr.exchange.api.model.AlertCheckResult;
import com.amr.exchange.api.model.AlertCheckItem;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Alerts implements Initializable {

    //create alert form
    @FXML public RadioButton usdToLbpRadio;
    @FXML public RadioButton lbpToUsdRadio;
    @FXML public TextField thresholdField;
    @FXML public RadioButton aboveRadio;
    @FXML public RadioButton belowRadio;
    @FXML public Button createButton;
    @FXML public Label createStatusLabel;

    //alerts table
    @FXML public TableView<Alert> alertsTable;
    @FXML public TableColumn<Alert, Integer> colId;
    @FXML public TableColumn<Alert, Boolean> colDirection;
    @FXML public TableColumn<Alert, Double>  colThreshold;
    @FXML public TableColumn<Alert, String>  colTrigger;
    @FXML public TableColumn<Alert, String>  colDate;
    @FXML public Label tableStatusLabel;

    //check results
    @FXML public TextArea checkResultArea;
    @FXML public Button checkButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadAlerts();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colThreshold.setCellValueFactory(new PropertyValueFactory<>("threshold"));
        colTrigger.setCellValueFactory(new PropertyValueFactory<>("direction"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("creationDate"));

        //direction column: readable text
        colDirection.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item ? "USD→LBP" : "LBP→USD");
            }
        });
        colDirection.setCellValueFactory(new PropertyValueFactory<>("usdToLbp"));

        //delete button column
        TableColumn<Alert, Void> colDelete = new TableColumn<>("Action");
        colDelete.setPrefWidth(80);
        colDelete.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setStyle(
                        "-fx-background-color:#e74c3c;-fx-text-fill:white;" +
                                "-fx-font-size:11px;-fx-padding:3 8 3 8;");
                deleteBtn.setOnAction(e -> {
                    Alert alert = getTableView().getItems().get(getIndex());
                    handleDelete(alert);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
        alertsTable.getColumns().add(colDelete);
    }

    //create alert
    @FXML
    public void onCreateAlert() {
        String thresholdStr = thresholdField.getText().trim();

        if (thresholdStr.isEmpty()) {
            setCreateStatus("Threshold is required.", "red");
            return;
        }
        if (!thresholdStr.matches("^\\d+(\\.\\d+)?$")) {
            setCreateStatus("Threshold must be a valid positive number.", "red");
            return;
        }

        double threshold = Double.parseDouble(thresholdStr);
        if (threshold <= 0) {
            setCreateStatus("Threshold must be greater than zero.", "red");
            return;
        }

        boolean usdToLbp = usdToLbpRadio.isSelected();
        String direction  = aboveRadio.isSelected() ? "above" : "below";
        String token = "Bearer " + Authentication.getInstance().getToken();

        createButton.setDisable(true);
        setCreateStatus("Creating alert...", "gray");

        Alert alert = new Alert(usdToLbp, threshold, direction);

        ExchangeService.exchangeApi()
                .createAlert(alert, token)
                .enqueue(new Callback<Alert>() {
                    @Override
                    public void onResponse(Call<Alert> call, Response<Alert> response) {
                        Platform.runLater(() -> {
                            createButton.setDisable(false);
                            if (response.isSuccessful()) {
                                setCreateStatus("✓ Alert created successfully!", "green");
                                thresholdField.clear();
                                loadAlerts();
                            } else if (response.code() == 400) {
                                setCreateStatus("Invalid input. Check your values.", "red");
                            } else if (response.code() == 401) {
                                setCreateStatus("Unauthorized. Please log in again.", "red");
                            } else {
                                setCreateStatus("Failed to create alert.", "red");
                            }
                        });
                    }
                    @Override
                    public void onFailure(Call<Alert> call, Throwable t) {
                        Platform.runLater(() -> {
                            createButton.setDisable(false);
                            setCreateStatus("Network error: is the backend running?", "red");
                        });
                    }
                });
    }

    //delete alert
    private void handleDelete(Alert alert) {
        String token = "Bearer " + Authentication.getInstance().getToken();
        tableStatusLabel.setText("Deleting alert #" + alert.id + "...");

        ExchangeService.exchangeApi()
                .deleteAlert(alert.id, token)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call,
                                           Response<ResponseBody> response) {
                        Platform.runLater(() -> {
                            if (response.isSuccessful()) {
                                tableStatusLabel.setText(
                                        "✓ Alert #" + alert.id + " deleted."
                                );
                                loadAlerts();
                            } else if (response.code() == 403) {
                                tableStatusLabel.setText(
                                        "You can only delete your own alerts."
                                );
                            } else if (response.code() == 404) {
                                tableStatusLabel.setText("Alert not found.");
                            } else {
                                tableStatusLabel.setText("Failed to delete alert.");
                            }
                        });
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Platform.runLater(() ->
                                tableStatusLabel.setText(
                                        "Network error: is the backend running?"
                                )
                        );
                    }
                });
    }

    //check alerts
    @FXML
    public void onCheckAlerts() {
        String token = "Bearer " + Authentication.getInstance().getToken();
        checkButton.setDisable(true);
        checkResultArea.setText("Checking alerts against current rates...");

        ExchangeService.exchangeApi()
                .checkAlerts(token)
                .enqueue(new Callback<AlertCheckResult>() {
                    @Override
                    public void onResponse(Call<AlertCheckResult> call,
                                           Response<AlertCheckResult> response) {
                        Platform.runLater(() -> {
                            checkButton.setDisable(false);
                            if (response.isSuccessful() && response.body() != null) {
                                AlertCheckResult result = response.body();
                                StringBuilder sb = new StringBuilder();

                                // current rates
                                sb.append("=== Current Rates ===\n");
                                sb.append(String.format("  USD → LBP: %s\n",
                                        result.currentUsdToLbpRate != null
                                                ? String.format("%.2f", result.currentUsdToLbpRate)
                                                : "No data"));
                                sb.append(String.format("  LBP → USD: %s\n",
                                        result.currentLbpToUsdRate != null
                                                ? String.format("%.2f", result.currentLbpToUsdRate)
                                                : "No data"));
                                sb.append("\n");

                                // triggered alerts
                                sb.append("=== Triggered Alerts ===\n");
                                if (result.triggeredAlerts == null
                                        || result.triggeredAlerts.isEmpty()) {
                                    sb.append("  None triggered.\n");
                                } else {
                                    for (AlertCheckItem a : result.triggeredAlerts) {
                                        sb.append(String.format(
                                                "  ⚠ Alert #%d | %s | %s %.2f" +
                                                        " | Current Rate: %.2f\n",
                                                a.id,
                                                a.usdToLbp ? "USD→LBP" : "LBP→USD",
                                                a.direction,
                                                a.threshold,
                                                a.currentRate != null ? a.currentRate : 0.0
                                        ));
                                    }
                                }
                                sb.append("\n");

                                // untriggered alerts
                                sb.append("=== Untriggered Alerts ===\n");
                                if (result.untriggeredAlerts == null
                                        || result.untriggeredAlerts.isEmpty()) {
                                    sb.append("  None.\n");
                                } else {
                                    for (AlertCheckItem a : result.untriggeredAlerts) {
                                        sb.append(String.format(
                                                "  Alert #%d | %s | %s %.2f" +
                                                        " | Current Rate: %.2f\n",
                                                a.id,
                                                a.usdToLbp ? "USD→LBP" : "LBP→USD",
                                                a.direction,
                                                a.threshold,
                                                a.currentRate != null ? a.currentRate : 0.0
                                        ));
                                    }
                                }

                                checkResultArea.setText(sb.toString());
                            } else if (response.code() == 401) {
                                checkResultArea.setText(
                                        "Unauthorized. Please log in again.");
                            } else {
                                checkResultArea.setText("Failed to check alerts.");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<AlertCheckResult> call, Throwable t) {
                        Platform.runLater(() -> {
                            checkButton.setDisable(false);
                            checkResultArea.setText(
                                    "Network error: is the backend running?");
                        });
                    }
                });
    }

    //load alerts
    private void loadAlerts() {
        String token = "Bearer " + Authentication.getInstance().getToken();
        tableStatusLabel.setText("Loading alerts...");

        ExchangeService.exchangeApi()
                .getAlerts(token)
                .enqueue(new Callback<List<Alert>>() {
                    @Override
                    public void onResponse(Call<List<Alert>> call,
                                           Response<List<Alert>> response) {
                        Platform.runLater(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Alert> list = response.body();
                                alertsTable.getItems().setAll(list);
                                tableStatusLabel.setText(
                                        list.isEmpty()
                                                ? "No alerts set."
                                                : list.size() + " alert(s) loaded."
                                );
                            } else {
                                tableStatusLabel.setText("Failed to load alerts.");
                            }
                        });
                    }
                    @Override
                    public void onFailure(Call<List<Alert>> call, Throwable t) {
                        Platform.runLater(() ->
                                tableStatusLabel.setText(
                                        "Network error: is the backend running?"
                                )
                        );
                    }
                });
    }

    private void setCreateStatus(String msg, String color) {
        createStatusLabel.setText(msg);
        createStatusLabel.setStyle("-fx-text-fill: " + color + ";");
    }
}