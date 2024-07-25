package org.rate.ratearestaurant;

import java.util.List;

import java.io.IOException;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class NewRestaurantController {
    @FXML
    private TextField type;
    @FXML
    private TextField name;
    @FXML
    private TextField address;
    @FXML
    private Label created;
    private DatabaseDriver databaseDriver;
    private User user;

    public void initialize(User user) throws SQLException {
        this.user = user;
    }

    @FXML
    public void validate() throws SQLException {
        databaseDriver = new DatabaseDriver("appTable.sqlite");
        databaseDriver.connect();

        String restaurantType = type.getText();
        String restaurantName = name.getText();
        String restaurantAddress = address.getText();

        boolean typeValid = true;
        char[] chars = restaurantType.toCharArray();
        for(char c : chars) {
            if(!Character.isLetter(c)) {
                typeValid = false;
            }
        }

        typeValid = !restaurantType.isEmpty() && restaurantType.length() <= 15;

        boolean nameValid = !restaurantName.isEmpty() && restaurantName.length() <= 50;

        boolean addressValid = !restaurantAddress.isEmpty() && restaurantAddress.length() <= 75;

        Restaurant restaurant = new Restaurant(restaurantType.toUpperCase(), restaurantAddress, restaurantName);

        boolean uniqueRestaurant = true;
        List<Restaurant> restaurants = databaseDriver.getAllRestaurants();
        for(Restaurant testRestaurant : restaurants) {
            if(testRestaurant.equals(restaurant)) {
                uniqueRestaurant = false;
            }
        }

        String errorMessage = "";
        if(!typeValid) {
            errorMessage += "The restaurant type is invalid. Do not use numerical or special characters, 1-15 characters long.\n";
        }
        if(!nameValid) {
            errorMessage += "The restaurant name is invalid. Please enter a name between 1-50 characters long.\n";
        }
        if(!addressValid) {
            errorMessage += "The restaurant address is invalid. Please enter an address between 1-75 characters long.\n";
        }
        if(!uniqueRestaurant) {
            errorMessage += "This restaurant is a duplicate of a pre-existing restaurant. Please enter a new restaurant.\n";
        }

        if(!typeValid || !nameValid || !addressValid || !uniqueRestaurant) {
            created.setText(errorMessage);
        }
        else{
            databaseDriver.addRestaurant(restaurant);
            databaseDriver.commit();

            created.setText("Restaurant added successfully!\nPlease return to the restaurant search page.");
        }

        databaseDriver.disconnect();
    }

    @FXML
    void backToRestaurantSearch() throws SQLException, IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RestaurantSearch.fxml"));
        Parent root = fxmlLoader.load();

        RestaurantSearchController controller = fxmlLoader.getController();
        controller.initialize(user);

        Stage stage = (Stage) created.getScene().getWindow();
        stage.setTitle("Restaurant Search");
        stage.setScene(new Scene(root));
    }

}