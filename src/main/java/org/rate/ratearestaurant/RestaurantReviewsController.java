package org.rate.ratearestaurant;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import java.io.IOException;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class RestaurantReviewsController {
    private DatabaseDriver databaseDriver;
    private User user;
    private Restaurant restaurant;
    @FXML
    private TableView top;
    @FXML
    private TableView bottom;
    @FXML
    private MenuItem back;

    public void initialize(Restaurant restaurant, User user) throws SQLException {
        this.user = user;
        this.restaurant = restaurant;

        databaseDriver = new DatabaseDriver("appTable.sqlite");
        topTable();
        updateReviewGraph();
    }

    void topTable() throws SQLException {
        databaseDriver = new DatabaseDriver("appTable.sqlite");
        databaseDriver.connect();

        List<Restaurant> allreviews = new LinkedList<>();
        allreviews.add(databaseDriver.getRestaurantById(restaurant.getID()));

        ObservableList<Restaurant> items = FXCollections.observableList(allreviews);;
        top.getItems().clear();
        top.getItems().addAll(items);

        databaseDriver.disconnect();
    }

    @FXML
    void updateReviewGraph() throws SQLException {
        databaseDriver = new DatabaseDriver("appTable.sqlite");
        databaseDriver.connect();

        List<Review> allReviews = databaseDriver.getReviewsForRestaurant(restaurant);
        ObservableList<Review> items=FXCollections.observableList(allReviews);

        bottom.getItems().clear();
        bottom.getItems().addAll(items);
        databaseDriver.disconnect();
    }

    @FXML
    void deleteReview() throws SQLException {
        databaseDriver.connect();

        ArrayList<Review> reviews = databaseDriver.getReviewsForRestaurant(this.restaurant);

        boolean already = false;

        for(int i = 0; i < reviews.size(); i++) {
            if(reviews.get(i).getUsername().equals(user.getUsername())) {
                already = true;
                break;
            }
        }

        if(already) {
            boolean happened= databaseDriver.deleteReviewForRestaurant(this.user, this.restaurant);

            databaseDriver.updateRestaurantAvg(this.restaurant);
            databaseDriver.disconnect();

            topTable();
            updateReviewGraph();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Your review for this restaurant has successfully been deleted. ");
            alert.showAndWait();
        }
        else {
            databaseDriver.disconnect();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("You have not created a review for this restaurant. \nThere is nothing to delete. ");
            alert.showAndWait();
        }
    }

    @FXML
    void openAddReview(ActionEvent event) throws SQLException {
        try {
            databaseDriver.connect();
        }
        catch(IllegalStateException ignored) {
        }

        ArrayList<Review> reviews = databaseDriver.getReviewsForRestaurant(this.restaurant);

        boolean already = false;

        for(int i = 0; i < reviews.size(); i++) {
            if(reviews.get(i).getUsername().equals(user.getUsername())) {
                already = true;
                break;
            }
        }

        if(already) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("You have already reviewed this restaurant. \nYou can either edit or delete your review. ");
            alert.showAndWait();
            databaseDriver.disconnect();
        }
        else {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Add a Restaurant Review");

            ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

            ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList(
                    "1 Star", "2 Stars", "3 Stars", "4 Stars", "5 Stars"
            ));

            TextField textField = new TextField();
            Label comboLabel = new Label("Rate the restaurant:");
            Label textLabel = new Label("Enter a comment (optional):");

            GridPane grid = new GridPane();
            grid.add(comboLabel, 0, 0);
            grid.add(comboBox, 1, 0);
            grid.add(textLabel, 0, 1);
            grid.add(textField, 1, 1);

            Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
            submitButton.disableProperty().bind(comboBox.valueProperty().isNull());

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if(dialogButton == submitButtonType) {
                    int stars = Character.getNumericValue(comboBox.getValue().charAt(0));
                    String text = textField.getText();
                    if(text.isEmpty()) {
                        text = "";
                    }

                    Review newReview = new Review(this.restaurant.getID(), stars, this.user.getUsername(), text);
                    try {
                        databaseDriver.addReview(newReview);
                        databaseDriver.updateRestaurantAvg(this.restaurant);
                        databaseDriver.disconnect();

                        topTable();
                        updateReviewGraph();
                    }
                    catch(SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                return null;
            });

            dialog.showAndWait().ifPresent(result -> {});
            databaseDriver.disconnect();
        }
    }

    @FXML
    void openEditReview(ActionEvent event) throws SQLException, IOException {
        try {
            databaseDriver.connect();
        }
        catch(IllegalStateException ignored) {

        }

        ArrayList<Review> reviews = databaseDriver.getReviewsForRestaurant(this.restaurant);

        Review oldReview = null;
        boolean already = false;

        for(int i = 0; i < reviews.size(); i++) {
            if(reviews.get(i).getUsername().equals(user.getUsername())) {
                already = true;
                oldReview = reviews.get(i);
                break;
            }
        }

        if(already) {
            databaseDriver.deleteReviewForRestaurant(this.user, this.restaurant);

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Edit Restaurant Review");

            ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

            ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList(
                    "1 Star", "2 Stars", "3 Stars", "4 Stars", "5 Stars"
            ));
            TextField textField = new TextField();
            Label comboLabel = new Label("Edit the rating:");
            Label textLabel = new Label("Edit the comment (optional):");

            String existingRating = String.valueOf(oldReview.getRating())+ " Star";
            comboBox.setValue(existingRating);
            textField.setText(oldReview.getComment());

            GridPane grid = new GridPane();
            grid.add(comboLabel, 0, 0);
            grid.add(comboBox, 1, 0);
            grid.add(textLabel, 0, 1);
            grid.add(textField, 1, 1);

            Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
            submitButton.disableProperty().bind(comboBox.valueProperty().isNull());

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if(dialogButton == submitButtonType) {
                    int stars = Character.getNumericValue(comboBox.getValue().charAt(0));
                    String text = textField.getText();
                    if(text.isEmpty()) {
                        text="";
                    }

                    Review newReview = new Review(this.restaurant.getID(), stars, this.user.getUsername(), text);

                    try {
                        databaseDriver.addReview(newReview);
                        databaseDriver.updateRestaurantAvg(this.restaurant);
                        databaseDriver.disconnect();

                        topTable();
                        updateReviewGraph();
                    }
                    catch(SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                return null;
            });

            dialog.showAndWait().ifPresent(result -> {});
        }
        else {
            databaseDriver.disconnect();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("You have not yet reviewed this restaurant. \nThere is nothing to edit. ");
            alert.showAndWait();
        }
    }

    @FXML
    void backtoSearch() throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RestaurantSearch.fxml"));
        Parent root = fxmlLoader.load();

        RestaurantSearchController controller = fxmlLoader.getController();
        controller.initialize(user);

        Stage stage = (Stage) top.getScene().getWindow();
        stage.setTitle("Restaurant Search");
        stage.setScene(new Scene(root));
    }

}