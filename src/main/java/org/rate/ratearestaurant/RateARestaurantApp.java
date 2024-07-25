package org.rate.ratearestaurant;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RateARestaurantApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseDriver databaseDriver = new DatabaseDriver("appTable.sqlite");
        databaseDriver.connect();
        databaseDriver.createTables();
        databaseDriver.commit();
        databaseDriver.disconnect();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("User Login");
        stage.setScene(scene);
        stage.show();
    }

}