package com.amr.exchange;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class Parent implements Initializable, OnPageCompleteListener {

    //we dont use @FXML, instead we set this manually from Main.java
    private BorderPane borderPane;

    @FXML public Button transactionButton;
    @FXML public Button exportButton;
    @FXML public Button loginButton;
    @FXML public Button registerButton;
    @FXML public Button logoutButton;
    @FXML public Label proofTimeLabel;
    @FXML public Label proofScreenLabel;

    //this is called by JavaFX automatically, buttons are injected here but borderPane isn't ready yet
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startClock();
        //we dont call swapContent here cz borderPane is null at this point
    }

    //this is called manually from Main.java after borderPane is set
    public void setBorderPane(BorderPane borderPane) {
        this.borderPane = borderPane;
    }

    public void initializeContent() {
        updateNavigation();
        if (Authentication.getInstance().getToken() != null) {
            swapContent(Section.DASHBOARD);
        } else {
            swapContent(Section.LOGIN);
        }
    }

    private void startClock() {
        Timer timer = new Timer(true);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                Platform.runLater(() ->
                        proofTimeLabel.setText("Time: " + LocalDateTime.now().format(fmt))
                );
            }
        }, 0, 1000);
    }

    @FXML public void dashboardSelected()    { swapContent(Section.DASHBOARD); }
    @FXML public void graphSelected()        { swapContent(Section.GRAPH); }
    @FXML public void transactionsSelected() { swapContent(Section.TRANSACTIONS); }
    @FXML public void exportSelected()       { swapContent(Section.EXPORT); }
    @FXML public void loginSelected()        { swapContent(Section.LOGIN); }
    @FXML public void registerSelected()     { swapContent(Section.REGISTER); }

    @FXML public void logoutSelected() {
        Authentication.getInstance().deleteToken();
        updateNavigation();
        swapContent(Section.LOGIN);
    }

    @Override
    public void onPageCompleted() {
        updateNavigation();
        swapContent(Section.DASHBOARD);
    }

    private void swapContent(Section section) {
        try {
            proofScreenLabel.setText("Screen: " + section.getLabel());
            URL url = getClass().getResource(section.getResource());
            FXMLLoader loader = new FXMLLoader(url);
            borderPane.setCenter(loader.load());

            if (section.doesComplete()) {
                ((PageCompleter) loader.getController())
                        .setOnPageCompleteListener(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateNavigation() {
        boolean auth = Authentication.getInstance().getToken() != null;
        transactionButton.setVisible(auth);  transactionButton.setManaged(auth);
        exportButton.setVisible(auth);       exportButton.setManaged(auth);
        loginButton.setVisible(!auth);       loginButton.setManaged(!auth);
        registerButton.setVisible(!auth);    registerButton.setManaged(!auth);
        logoutButton.setVisible(auth);       logoutButton.setManaged(auth);
    }

    private enum Section {
        DASHBOARD, GRAPH, TRANSACTIONS, EXPORT, LOGIN, REGISTER;

        public String getResource() {
            return switch (this) {
                case DASHBOARD    -> "dashboard/dashboard.fxml";
                case GRAPH        -> "login/login.fxml";   // placeholder
                case TRANSACTIONS -> "login/login.fxml";   // placeholder
                case EXPORT       -> "login/login.fxml";   // placeholder
                case LOGIN        -> "login/login.fxml";
                case REGISTER     -> "register/register.fxml";
            };
        }

        public String getLabel() {
            return switch (this) {
                case DASHBOARD    -> "Dashboard";
                case GRAPH        -> "Graph";
                case TRANSACTIONS -> "Transactions";
                case EXPORT       -> "Export CSV";
                case LOGIN        -> "Login";
                case REGISTER     -> "Register";
            };
        }

        public boolean doesComplete() {
            return this == LOGIN || this == REGISTER;
        }
    }
}