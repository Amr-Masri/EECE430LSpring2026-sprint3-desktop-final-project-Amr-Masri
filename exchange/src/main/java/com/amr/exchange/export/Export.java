package com.amr.exchange.export;

import com.amr.exchange.Authentication;
import com.amr.exchange.api.ExchangeService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Export implements Initializable {

    @FXML public Button exportButton;
    @FXML public Label statusLabel;
    @FXML public TextArea previewArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        previewArea.setEditable(false);
        previewArea.setPromptText(
                "CSV preview will appear here after export..."
        );
        statusLabel.setText(
                "Click the button below to export your transaction history as CSV."
        );
    }

    @FXML
    public void onExport() {
        String token = "Bearer " + Authentication.getInstance().getToken();

        exportButton.setDisable(true);
        statusLabel.setText("Fetching your transactions...");
        statusLabel.setStyle("-fx-text-fill: gray;");
        previewArea.clear();

        ExchangeService.exchangeApi()
                .exportTransactions(token)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call,
                                           Response<ResponseBody> response) {
                        Platform.runLater(() -> {
                            exportButton.setDisable(false);

                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String csvContent = response.body().string();

                                    //check if backend returned empty message
                                    if (csvContent.trim().startsWith("{")) {
                                        statusLabel.setText(
                                                "No transactions to export yet."
                                        );
                                        statusLabel.setStyle("-fx-text-fill: orange;");
                                        return;
                                    }

                                    // show preview in the text area
                                    previewArea.setText(csvContent);

                                    //open file chooser so user can pick teh save location
                                    saveToFile(csvContent);

                                } catch (IOException e) {
                                    statusLabel.setText(
                                            "Error reading response: " + e.getMessage()
                                    );
                                    statusLabel.setStyle("-fx-text-fill: red;");
                                }

                            } else if (response.code() == 401) {
                                statusLabel.setText(
                                        "Unauthorized. Please log in again."
                                );
                                statusLabel.setStyle("-fx-text-fill: red;");
                            } else {
                                statusLabel.setText(
                                        "Export failed. Try again later."
                                );
                                statusLabel.setStyle("-fx-text-fill: red;");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Platform.runLater(() -> {
                            exportButton.setDisable(false);
                            statusLabel.setText(
                                    "Network error: is the backend running?"
                            );
                            statusLabel.setStyle("-fx-text-fill: red;");
                        });
                    }
                });
    }

    private void saveToFile(String csvContent) {
        //open a save dialog so the user picks where to save
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Transactions CSV");
        fileChooser.setInitialFileName("transactions.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        //get the current window as the owner of the dialog
        Stage stage = (Stage) exportButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(csvContent);
                statusLabel.setText(
                        "✓ Exported successfully to: " + file.getAbsolutePath()
                );
                statusLabel.setStyle("-fx-text-fill: green;");
            } catch (IOException e) {
                statusLabel.setText(
                        "Failed to save file: " + e.getMessage()
                );
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } else {
            //user cancelled the save dialog
            statusLabel.setText(
                    "Export cancelled. Preview is still shown below."
            );
            statusLabel.setStyle("-fx-text-fill: orange;");
        }
    }
}