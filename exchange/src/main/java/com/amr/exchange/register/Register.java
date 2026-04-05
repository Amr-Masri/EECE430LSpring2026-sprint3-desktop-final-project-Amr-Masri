package com.amr.exchange.register;

import com.amr.exchange.Authentication;
import com.amr.exchange.OnPageCompleteListener;
import com.amr.exchange.PageCompleter;
import com.amr.exchange.api.ExchangeService;
import com.amr.exchange.api.model.Token;
import com.amr.exchange.api.model.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register implements PageCompleter {
    @FXML public TextField usernameTextField;
    @FXML public PasswordField passwordTextField;
    @FXML public Label errorLabel;

    private OnPageCompleteListener onPageCompleteListener;

    @Override
    public void setOnPageCompleteListener(OnPageCompleteListener listener) {
        this.onPageCompleteListener = listener;
    }

    @FXML
    public void register(ActionEvent actionEvent) {
        String username = usernameTextField.getText().trim();
        String password = passwordTextField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password are required.");
            return;
        }

        errorLabel.setText("");
        User user = new User(username, password);

        ExchangeService.exchangeApi().addUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // auto-login after register
                    ExchangeService.exchangeApi().authenticate(user).enqueue(new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            Platform.runLater(() -> {
                                if (response.isSuccessful() && response.body() != null) {
                                    Authentication.getInstance().saveToken(response.body().getToken());
                                    if (onPageCompleteListener != null) {
                                        onPageCompleteListener.onPageCompleted();
                                    }
                                } else {
                                    errorLabel.setText("Registered but login failed. Try logging in.");
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
                } else {
                    Platform.runLater(() -> {
                        if (response.code() == 400) {
                            errorLabel.setText("Username already taken.");
                        } else {
                            errorLabel.setText("Registration failed. Try again.");
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable throwable) {
                Platform.runLater(() ->
                        errorLabel.setText("Network error. Is the backend running?")
                );
            }
        });
    }
}