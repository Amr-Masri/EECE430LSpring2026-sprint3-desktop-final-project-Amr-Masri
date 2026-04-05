module com.yourname.exchange {
    requires javafx.controls;
    requires javafx.fxml;
    requires retrofit2;
    requires java.sql;
    requires gson;
    requires retrofit2.converter.gson;
    requires java.prefs;

    opens com.amr.exchange to javafx.fxml;
    opens com.amr.exchange.api.model to gson;
    exports com.amr.exchange;
}