package com.amr.exchange.login;

import com.amr.exchange.Authentication;
import com.amr.exchange.OnPageCompleteListener;
import com.amr.exchange.PageCompleter;
import com.amr.exchange.api.ExchangeService;
import com.amr.exchange.api.model.Token;
import com.amr.exchange.api.model.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login implements PageCompleter {
    @FXML public TextField usernameTextField;
    @FXML public PasswordField passwordTextField;
    @FXML public Label errorLabel;

    private OnPageCompleteListener onPageCompleteListener;

    @Override
    public void setOnPageCompleteListener(OnPageCompleteListener listener) {
        this.onPageCompleteListener = listener;
    }

    @FXML
    public void login(ActionEvent actionEvent) {
        String username = usernameTextField.getText().trim();
        String password = passwordTextField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password are required.");
            return;
        }

        errorLabel.setText("");
        User user = new User(username, password);

        ExchangeService.exchangeApi().authenticate(user).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                Platform.runLater(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        Authentication.getInstance().saveToken(response.body().getToken());
                        if (onPageCompleteListener != null) {
                            onPageCompleteListener.onPageCompleted();
                        }
                    } else if (response.code() == 429) {
                        errorLabel.setText("Too many attempts. Please wait and try again.");
                    } else {
                        errorLabel.setText("Invalid credentials or account suspended.");
                    }
                });
            }

            @Override
            public void onFailure(Call<Token> call, Throwable throwable) {
                Platform.runLater(() ->
                        errorLabel.setText("Network error. Is the backend running?")
                );
            }
        });
    }
}