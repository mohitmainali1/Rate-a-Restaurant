package org.rate.ratearestaurant;

import java.util.ArrayList;

import java.io.IOException;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class MyReviewsController {
    private DatabaseDriver databaseDriver;
    private User user;
    @FXML
    private MenuItem back;
    @FXML
    public TableView<ReviewsListed> tableView;

    public void initialize(User user) throws SQLException {
        databaseDriver = new DatabaseDriver("appTable.sqlite");
        this.user = user;
        initReviews();
    }

    @FXML
    private void initReviews() throws SQLException {
        databaseDriver.connect();

        ArrayList<Review> reviews = databaseDriver.getReviewsForUser(user);
        ArrayList<ReviewsListed> formattedReviews = new ArrayList<>();
        for(Review review : reviews) {
            ReviewsListed reviewFormatted = new ReviewsListed(review);
            formattedReviews.add(reviewFormatted);
        }

        ObservableList<ReviewsListed> items = FXCollections.observableList(formattedReviews);

        tableView.getItems().clear();
        tableView.getItems().addAll(items);
        databaseDriver.disconnect();

        tableView.setOnMouseClicked(event -> {
            if(event.getClickCount() == 1) {
                ReviewsListed selectedReview = tableView.getSelectionModel().getSelectedItem();

                if(selectedReview != null) {
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
                        databaseDriver.connect();
                        Restaurant selectedRestaurant = databaseDriver.getRestaurantById(selectedReview.getRestaurantID());
                        databaseDriver.disconnect();
                        controller.initialize(selectedRestaurant, user);
                    }
                    catch (SQLException e) {
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
    void backToRestaurantSearch() throws SQLException, IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RestaurantSearch.fxml"));
        Parent root = fxmlLoader.load();

        RestaurantSearchController controller = fxmlLoader.getController();
        controller.initialize(user);

        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.setTitle("Restaurant Search");
        stage.setScene(new Scene(root));
    }

}