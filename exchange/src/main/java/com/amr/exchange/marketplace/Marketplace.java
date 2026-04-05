package com.amr.exchange.marketplace;

import com.amr.exchange.Authentication;
import com.amr.exchange.api.ExchangeService;
import com.amr.exchange.api.model.Offer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Marketplace implements Initializable {

    //create offer form
    @FXML public TextField createUsdField;
    @FXML public TextField createLbpField;
    @FXML public RadioButton createUsdToLbpRadio;
    @FXML public RadioButton createLbpToUsdRadio;
    @FXML public Button createButton;
    @FXML public Label createStatusLabel;

    //available offers table
    @FXML public TableView<Offer> offersTable;
    @FXML public TableColumn<Offer, Integer> colOfferId;
    @FXML public TableColumn<Offer, Integer> colOfferUser;
    @FXML public TableColumn<Offer, Double>  colOfferUsd;
    @FXML public TableColumn<Offer, Double>  colOfferLbp;
    @FXML public TableColumn<Offer, Boolean> colOfferDir;
    @FXML public TableColumn<Offer, String>  colOfferDate;
    @FXML public Label offersStatusLabel;

    //my trades table
    @FXML public TableView<Offer> tradesTable;
    @FXML public TableColumn<Offer, Integer> colTradeId;
    @FXML public TableColumn<Offer, Double>  colTradeUsd;
    @FXML public TableColumn<Offer, Double>  colTradeLbp;
    @FXML public TableColumn<Offer, Boolean> colTradeDir;
    @FXML public TableColumn<Offer, String>  colTradeStatus;
    @FXML public TableColumn<Offer, String>  colTradeDate;
    @FXML public Label tradesStatusLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupOffersTable();
        setupTradesTable();
        loadAvailableOffers();
        loadMyTrades();
    }

    //table setup
    private void setupOffersTable() {
        colOfferId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOfferUser.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colOfferUsd.setCellValueFactory(new PropertyValueFactory<>("usdAmount"));
        colOfferLbp.setCellValueFactory(new PropertyValueFactory<>("lbpAmount"));
        colOfferDate.setCellValueFactory(new PropertyValueFactory<>("creationDate"));

        colOfferDir.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item ? "USD→LBP" : "LBP→USD");
            }
        });
        colOfferDir.setCellValueFactory(new PropertyValueFactory<>("usdToLbp"));

        //action column for Accept or Cancel buttons
        TableColumn<Offer, Void> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(130);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button acceptBtn = new Button("Accept");
            private final Button cancelBtn = new Button("Cancel");
            private final javafx.scene.layout.HBox box =
                    new javafx.scene.layout.HBox(6, acceptBtn, cancelBtn);

            {
                acceptBtn.setStyle(
                        "-fx-background-color:#27ae60;-fx-text-fill:white;" +
                                "-fx-font-size:11px;-fx-padding:3 8 3 8;");
                cancelBtn.setStyle(
                        "-fx-background-color:#e74c3c;-fx-text-fill:white;" +
                                "-fx-font-size:11px;-fx-padding:3 8 3 8;");

                acceptBtn.setOnAction(e -> {
                    Offer offer = getTableView().getItems().get(getIndex());
                    handleAccept(offer);
                });
                cancelBtn.setOnAction(e -> {
                    Offer offer = getTableView().getItems().get(getIndex());
                    handleCancel(offer);
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
        offersTable.getColumns().add(colAction);
    }

    private void setupTradesTable() {
        colTradeId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTradeUsd.setCellValueFactory(new PropertyValueFactory<>("usdAmount"));
        colTradeLbp.setCellValueFactory(new PropertyValueFactory<>("lbpAmount"));
        colTradeStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTradeDate.setCellValueFactory(new PropertyValueFactory<>("acceptedAt"));

        colTradeDir.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item ? "USD→LBP" : "LBP→USD");
            }
        });
        colTradeDir.setCellValueFactory(new PropertyValueFactory<>("usdToLbp"));
    }

    //create offer
    @FXML
    public void onCreateOffer() {
        String usdStr = createUsdField.getText().trim();
        String lbpStr = createLbpField.getText().trim();

        if (usdStr.isEmpty() || lbpStr.isEmpty()) {
            setCreateStatus("Both USD and LBP amounts are required.", "red");
            return;
        }
        if (!usdStr.matches("^\\d+(\\.\\d{1,2})?$") ||
                !lbpStr.matches("^\\d+(\\.\\d{1,2})?$")) {
            setCreateStatus("Amounts must be valid positive numbers.", "red");
            return;
        }

        double usd = Double.parseDouble(usdStr);
        double lbp = Double.parseDouble(lbpStr);

        if (usd <= 0 || lbp <= 0) {
            setCreateStatus("Amounts must be greater than zero.", "red");
            return;
        }

        boolean usdToLbp = createUsdToLbpRadio.isSelected();
        String token = "Bearer " + Authentication.getInstance().getToken();

        createButton.setDisable(true);
        setCreateStatus("Creating offer...", "gray");

        Offer offer = new Offer(usd, lbp, usdToLbp);

        ExchangeService.exchangeApi()
                .createOffer(offer, token)
                .enqueue(new Callback<Offer>() {
                    @Override
                    public void onResponse(Call<Offer> call, Response<Offer> response) {
                        Platform.runLater(() -> {
                            createButton.setDisable(false);
                            if (response.isSuccessful()) {
                                setCreateStatus("✓ Offer created successfully!", "green");
                                createUsdField.clear();
                                createLbpField.clear();
                                loadAvailableOffers();
                            } else if (response.code() == 401) {
                                setCreateStatus("Unauthorized. Please log in again.", "red");
                            } else {
                                setCreateStatus("Failed to create offer. Check your input.", "red");
                            }
                        });
                    }
                    @Override
                    public void onFailure(Call<Offer> call, Throwable t) {
                        Platform.runLater(() -> {
                            createButton.setDisable(false);
                            setCreateStatus("Network error: is the backend running?", "red");
                        });
                    }
                });
    }

    //accept offer
    private void handleAccept(Offer offer) {
        String token = "Bearer " + Authentication.getInstance().getToken();
        offersStatusLabel.setText("Accepting offer #" + offer.id + "...");

        ExchangeService.exchangeApi()
                .acceptOffer(offer.id, token)
                .enqueue(new Callback<Offer>() {
                    @Override
                    public void onResponse(Call<Offer> call, Response<Offer> response) {
                        Platform.runLater(() -> {
                            if (response.isSuccessful()) {
                                offersStatusLabel.setText(
                                        "✓ Offer #" + offer.id + " accepted successfully!"
                                );
                                loadAvailableOffers();
                                loadMyTrades();
                            } else if (response.code() == 400) {
                                offersStatusLabel.setText(
                                        "Cannot accept: offer no longer available or it's your own offer."
                                );
                            } else if (response.code() == 429) {
                                offersStatusLabel.setText(
                                        "Too many offers accepted. Wait and try again."
                                );
                            } else if (response.code() == 401) {
                                offersStatusLabel.setText("Unauthorized. Please log in again.");
                            } else {
                                offersStatusLabel.setText("Failed to accept offer.");
                            }
                        });
                    }
                    @Override
                    public void onFailure(Call<Offer> call, Throwable t) {
                        Platform.runLater(() ->
                                offersStatusLabel.setText("Network error: is the backend running?")
                        );
                    }
                });
    }

    //cancel offer
    private void handleCancel(Offer offer) {
        String token = "Bearer " + Authentication.getInstance().getToken();
        offersStatusLabel.setText("Canceling offer #" + offer.id + "...");

        ExchangeService.exchangeApi()
                .cancelOffer(offer.id, token)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call,
                                           Response<ResponseBody> response) {
                        Platform.runLater(() -> {
                            if (response.isSuccessful()) {
                                offersStatusLabel.setText(
                                        "✓ Offer #" + offer.id + " canceled."
                                );
                                loadAvailableOffers();
                                loadMyTrades();
                            } else if (response.code() == 403) {
                                offersStatusLabel.setText(
                                        "You can only cancel your own offers."
                                );
                            } else if (response.code() == 400) {
                                offersStatusLabel.setText(
                                        "Only available offers can be canceled."
                                );
                            } else {
                                offersStatusLabel.setText("Failed to cancel offer.");
                            }
                        });
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Platform.runLater(() ->
                                offersStatusLabel.setText("Network error: is the backend running?")
                        );
                    }
                });
    }

    //load data
    private void loadAvailableOffers() {
        offersStatusLabel.setText("Loading offers...");
        ExchangeService.exchangeApi()
                .getAvailableOffers()
                .enqueue(new Callback<List<Offer>>() {
                    @Override
                    public void onResponse(Call<List<Offer>> call,
                                           Response<List<Offer>> response) {
                        Platform.runLater(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Offer> offers = response.body();
                                offersTable.getItems().setAll(offers);
                                offersStatusLabel.setText(
                                        offers.isEmpty()
                                                ? "No open offers available."
                                                : offers.size() + " offer(s) available."
                                );
                            } else {
                                offersStatusLabel.setText("Failed to load offers.");
                            }
                        });
                    }
                    @Override
                    public void onFailure(Call<List<Offer>> call, Throwable t) {
                        Platform.runLater(() ->
                                offersStatusLabel.setText("Network error: is the backend running?")
                        );
                    }
                });
    }

    private void loadMyTrades() {
        String token = "Bearer " + Authentication.getInstance().getToken();
        tradesStatusLabel.setText("Loading trade history...");

        ExchangeService.exchangeApi()
                .getMyTrades(token)
                .enqueue(new Callback<List<Offer>>() {
                    @Override
                    public void onResponse(Call<List<Offer>> call,
                                           Response<List<Offer>> response) {
                        Platform.runLater(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Offer> trades = response.body();
                                tradesTable.getItems().setAll(trades);
                                tradesStatusLabel.setText(
                                        trades.isEmpty()
                                                ? "No completed trades yet."
                                                : trades.size() + " trade(s) found."
                                );
                            } else {
                                tradesStatusLabel.setText("Failed to load trades.");
                            }
                        });
                    }
                    @Override
                    public void onFailure(Call<List<Offer>> call, Throwable t) {
                        Platform.runLater(() ->
                                tradesStatusLabel.setText("Network error: is the backend running?")
                        );
                    }
                });
    }

    private void setCreateStatus(String msg, String color) {
        createStatusLabel.setText(msg);
        createStatusLabel.setStyle("-fx-text-fill: " + color + ";");
    }
}