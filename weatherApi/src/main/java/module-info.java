module com.example.weatherapi {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;


    opens com.example.weatherapi to javafx.fxml;
    exports com.example.weatherapi;
}