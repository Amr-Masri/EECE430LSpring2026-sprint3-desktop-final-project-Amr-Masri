module com.amr.exchange {
    requires javafx.controls;
    requires javafx.fxml;
    requires retrofit2;
    requires java.sql;
    requires gson;
    requires retrofit2.converter.gson;
    requires java.prefs;

    opens com.amr.exchange to javafx.fxml;
    opens com.amr.exchange.api.model to javafx.base, gson;
    opens com.amr.exchange.login to javafx.fxml;
    opens com.amr.exchange.register to javafx.fxml;
    opens com.amr.exchange.dashboard to javafx.fxml;
    opens com.amr.exchange.graph to javafx.fxml;
    opens com.amr.exchange.transactions to javafx.fxml;
    exports com.amr.exchange;
}