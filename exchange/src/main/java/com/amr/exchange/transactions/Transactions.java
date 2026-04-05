package com.amr.exchange.transactions;

import com.amr.exchange.Authentication;
import com.amr.exchange.api.ExchangeService;
import com.amr.exchange.api.model.Transaction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Transactions implements Initializable {

    //add transaction form
    @FXML public TextField usdAmountField;
    @FXML public TextField lbpAmountField;
    @FXML public RadioButton usdToLbpRadio;
    @FXML public RadioButton lbpToUsdRadio;
    @FXML public Button addButton;
    @FXML public Label formStatusLabel;

    //transaction history table
    @FXML public TableView<Transaction> transactionTable;
    @FXML public TableColumn<Transaction, Integer> colId;
    @FXML public TableColumn<Transaction, Double>  colUsd;
    @FXML public TableColumn<Transaction, Double>  colLbp;
    @FXML public TableColumn<Transaction, Boolean> colDirection;
    @FXML public TableColumn<Transaction, String>  colDate;
    @FXML public TableColumn<Transaction, Boolean> colOutlier;
    @FXML public Label tableStatusLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadTransactions();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsd.setCellValueFactory(new PropertyValueFactory<>("usdAmount"));
        colLbp.setCellValueFactory(new PropertyValueFactory<>("lbpAmount"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("addedDate"));

        colDirection.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "USD → LBP" : "LBP → USD");
                }
            }
        });
        colDirection.setCellValueFactory(new PropertyValueFactory<>("usdToLbp"));

        //show outlier as Yes/No with red highlight
        colOutlier.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else if (item) {
                    setText("⚠ Yes");
                    setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else {
                    setText("No");
                    setStyle("");
                }
            }
        });
        colOutlier.setCellValueFactory(new PropertyValueFactory<>("isOutlier"));
    }

    //add transaction
    @FXML
    public void onAddTransaction() {
        String usdStr = usdAmountField.getText().trim();
        String lbpStr = lbpAmountField.getText().trim();

        //client-side validation
        if (usdStr.isEmpty() || lbpStr.isEmpty()) {
            formStatusLabel.setText("Both USD and LBP amounts are required.");
            formStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        if (!usdStr.matches("^\\d+(\\.\\d{1,2})?$") ||
                !lbpStr.matches("^\\d+(\\.\\d{1,2})?$")) {
            formStatusLabel.setText("Amounts must be valid positive numbers.");
            formStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        double usdAmount = Double.parseDouble(usdStr);
        double lbpAmount = Double.parseDouble(lbpStr);

        if (usdAmount <= 0 || lbpAmount <= 0) {
            formStatusLabel.setText("Amounts must be greater than zero.");
            formStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean usdToLbp = usdToLbpRadio.isSelected();
        String token = "Bearer " + Authentication.getInstance().getToken();

        addButton.setDisable(true);
        formStatusLabel.setText("Submitting...");
        formStatusLabel.setStyle("-fx-text-fill: gray;");

        Transaction transaction = new Transaction(usdAmount, lbpAmount, usdToLbp);

        ExchangeService.exchangeApi()
                .addTransaction(transaction, token)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        Platform.runLater(() -> {
                            addButton.setDisable(false);
                            if (response.isSuccessful()) {
                                formStatusLabel.setText(
                                        "✓ Transaction added successfully!"
                                );
                                formStatusLabel.setStyle("-fx-text-fill: green;");
                                usdAmountField.clear();
                                lbpAmountField.clear();
                                loadTransactions(); // refresh the table
                            } else if (response.code() == 429) {
                                formStatusLabel.setText(
                                        "Too many requests. Wait and try again."
                                );
                                formStatusLabel.setStyle("-fx-text-fill: red;");
                            } else if (response.code() == 401) {
                                formStatusLabel.setText(
                                        "Unauthorized. Please log in again."
                                );
                                formStatusLabel.setStyle("-fx-text-fill: red;");
                            } else {
                                formStatusLabel.setText(
                                        "Failed to add transaction (400). Check your input."
                                );
                                formStatusLabel.setStyle("-fx-text-fill: red;");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Platform.runLater(() -> {
                            addButton.setDisable(false);
                            formStatusLabel.setText(
                                    "Network error: is the backend running?"
                            );
                            formStatusLabel.setStyle("-fx-text-fill: red;");
                        });
                    }
                });
    }

    //load historyy
    private void loadTransactions() {
        String token = "Bearer " + Authentication.getInstance().getToken();
        tableStatusLabel.setText("Loading transactions...");

        ExchangeService.exchangeApi()
                .getUserTransactions(token)
                .enqueue(new Callback<List<Transaction>>() {
                    @Override
                    public void onResponse(Call<List<Transaction>> call,
                                           Response<List<Transaction>> response) {
                        Platform.runLater(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Transaction> list = response.body();
                                transactionTable.getItems().setAll(list);
                                tableStatusLabel.setText(
                                        list.isEmpty()
                                                ? "No transactions found."
                                                : list.size() + " transaction(s) loaded."
                                );
                            } else if (response.code() == 401) {
                                tableStatusLabel.setText(
                                        "Unauthorized. Please log in again."
                                );
                            } else {
                                tableStatusLabel.setText("Failed to load transactions.");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<Transaction>> call, Throwable t) {
                        Platform.runLater(() ->
                                tableStatusLabel.setText(
                                        "Network error: is the backend running?"
                                )
                        );
                    }
                });
    }
}