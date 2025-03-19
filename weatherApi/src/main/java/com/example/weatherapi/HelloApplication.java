package com.example.weatherapi;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.json.JSONObject;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HelloApplication extends Application {

    private static final String API_KEY = "klucz_api";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&lang=pl";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("API Pogody");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label titleLabel = new Label("Miasto:");
        titleLabel.setStyle("-fx-font-size: 24px;");

        TextField cityInput = new TextField();
        cityInput.setPromptText("Wpisz nazwę miasta");

        Button searchButton = new Button("Szukaj");
        searchButton.setGraphic(new ImageView(new Image("https://image.shutterstock.com/image-vector/magnifying-glass-icon-vector-illustration-260nw-1754470529.jpg")));

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);

        ImageView weatherIcon = new ImageView();
        weatherIcon.setFitHeight(50);
        weatherIcon.setFitWidth(50);

        searchButton.setOnAction(e -> {
            String cityName = cityInput.getText();
            if (!cityName.isEmpty()) {
                fetchWeatherData(cityName, resultArea, weatherIcon);
            }
        });

        Button closeButton = new Button("Zamknij");
        closeButton.setOnAction(e -> primaryStage.close());

        HBox header = new HBox(10, weatherIcon, titleLabel);
        HBox footer = new HBox(10, closeButton);

        root.getChildren().addAll(header, cityInput, searchButton, weatherIcon, resultArea, footer);

        Scene scene = new Scene(root, 300, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void fetchWeatherData(String cityName, TextArea resultArea, ImageView weatherIcon) {
        try {
            String urlString = String.format(API_URL, cityName, API_KEY);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            displayWeatherData(jsonResponse, resultArea, weatherIcon);

        } catch (Exception e) {
            resultArea.setText("Błąd połączenia z API.");
            e.printStackTrace();
        }
    }

    private void displayWeatherData(JSONObject jsonResponse, TextArea resultArea, ImageView weatherIcon) {
        try {
            JSONObject weather = jsonResponse.getJSONArray("weather").getJSONObject(0);
            JSONObject main = jsonResponse.getJSONObject("main");
            JSONObject wind = jsonResponse.getJSONObject("wind");
            JSONObject clouds = jsonResponse.getJSONObject("clouds");

            JSONObject rain = jsonResponse.optJSONObject("rain");
            JSONObject snow = jsonResponse.optJSONObject("snow");

            String description = weather.getString("description");
            String icon = weather.getString("icon");
            double temperature = main.getDouble("temp") - 273.15;
            int humidity = main.getInt("humidity");
            double windSpeed = wind.getDouble("speed");
            int windDegree = wind.optInt("deg", -1);
            int cloudiness = clouds.getInt("all");
            int visibility = jsonResponse.optInt("visibility", -1);
            String cityName = jsonResponse.getString("name");

            StringBuilder resultText = new StringBuilder();
            resultText.append("Miasto: ").append(cityName).append("\n")
                    .append("Pogoda: ").append(description).append("\n")
                    .append("Temperatura: ").append(String.format("%.1f", temperature)).append(" °C\n")
                    .append("Wilgotność: ").append(humidity).append(" %\n")
                    .append("Prędkość wiatru: ").append(windSpeed).append(" m/s\n")
                    .append("Kierunek wiatru: ").append(windDegree).append("°\n")
                    .append("Widoczność: ").append(visibility / 1000.0).append(" km\n")
                    .append("Zachmurzenie: ").append(cloudiness).append(" %\n")
                    .append("Opady deszczu: ").append(rain != null ? rain.optDouble("1h", 0.0) : 0.0).append(" mm/h\n")
                    .append("Opady śniegu: ").append(snow != null ? snow.optDouble("1h", 0.0) : 0.0).append(" mm/h\n");

            String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";
            weatherIcon.setImage(new Image(iconUrl));

            resultArea.setText(resultText.toString());
        } catch (Exception e) {
            resultArea.setText("Błąd podczas parsowania danych.");
            e.printStackTrace();
        }
    }
}
