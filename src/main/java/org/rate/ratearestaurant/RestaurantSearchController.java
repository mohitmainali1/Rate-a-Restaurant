package org.rate.ratearestaurant;

import java.util.List;

import java.io.IOException;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RestaurantSearchController {
    private User user;
    private DatabaseDriver databaseDriver;
    @FXML
    public TableView<Restaurant> tableView;
    @FXML
    private Button search;
    @FXML
    private TextField type, name, address;

    public void initialize(User user) throws SQLException {
        this.user = user;
        initRestaurants();
    }

    private void initRestaurants() throws SQLException {
        databaseDriver = new DatabaseDriver("appTable.sqlite");
        databaseDriver.connect();

        List<Restaurant> restaurants = databaseDriver.getAllRestaurants();
        ObservableList<Restaurant> items=FXCollections.observableList(restaurants);

        tableView.getItems().clear();
        tableView.getItems().addAll(items);
        databaseDriver.disconnect();

        tableView.setOnMouseClicked(event -> {
            if(event.getClickCount() == 1) {
                Restaurant selectedRestaurant = tableView.getSelectionModel().getSelectedItem();

                if(selectedRestaurant != null) {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RestaurantReviews.fxml"));
                    Parent root = null;

                    try {
                        root = fxmlLoader.load();
                    }
                    catch(IOException e) {
                        throw new RuntimeException(e);
                    }

                    RestaurantReviewsController controller = fxmlLoader.getController();
                    try {
                        controller.initialize(selectedRestaurant, user);
                    }
                    catch(SQLException e) {
                        throw new RuntimeException(e);
                    }

                    Stage stage = (Stage) tableView.getScene().getWindow();
                    stage.setTitle("Restaurant Reviews");
                    stage.setScene(new Scene(root));
                }
            }
        });
    }

    @FXML
    public void logout() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Login.fxml"));

        Stage stage = (Stage) search.getScene().getWindow();
        stage.setTitle("Login");
        stage.setScene(new Scene(fxmlLoader.load()));
    }

    @FXML
    public void myReviews() throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MyReviews.fxml"));
        Parent root = fxmlLoader.load();

        MyReviewsController controller = fxmlLoader.getController();
        controller.initialize(user);

        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.setTitle("My Reviews");
        stage.setScene(new Scene(root));
    }

    @FXML
    private void SearchCourseByAll() throws SQLException {
        databaseDriver = new DatabaseDriver("appTable.sqlite");
        databaseDriver.connect();

        var location = address.getText();
        var nm = name.getText();
        var tp = type.getText();

        List<Restaurant> restaurants = databaseDriver.getRestaurantsByAll(tp, nm, location);
        ObservableList<Restaurant> items=FXCollections.observableList(restaurants);;
        tableView.getItems().clear();
        tableView.getItems().addAll(items);

        databaseDriver.disconnect();
    }

    @FXML
    private void goToNewRestaurant() throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NewRestaurant.fxml"));
        Parent root = fxmlLoader.load();

        NewRestaurantController controller = fxmlLoader.getController();
        controller.initialize(user);

        Stage stage = (Stage) search.getScene().getWindow();
        stage.setTitle("Add a Restaurant");
        stage.setScene(new Scene(root));
    }

}