package org.rate.ratearestaurant;

import java.util.Optional;
import java.io.IOException;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.*;

public class LoginController {
    private DatabaseDriver databaseDriver;
    @FXML
    private Label messageLabel;
    @FXML
    private TextField Username;
    @FXML
    private PasswordField Password;
    @FXML
    private Button create;

    public void initialize() throws SQLException {
        databaseDriver = new DatabaseDriver("appTable.sqlite");
    }

    @FXML
    public void createUser() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NewUser.fxml"));
        Stage stage = (Stage) create.getScene().getWindow();
        stage.setTitle("User Registration");
        stage.setScene(new Scene(fxmlLoader.load()));
    }

    @FXML
    public void logPress() throws SQLException, IOException {
        databaseDriver.connect();

        String usernameText = Username.getText();
        String passwordText = Password.getText();

        Optional<User> userLogin = databaseDriver.getUserById(usernameText);
        if(userLogin.isEmpty()) {
            messageLabel.setText("Unknown username");
        }
        else {
            if(userLogin.get().getPassword().equals(passwordText)) {
                messageLabel.setText("Welcome!");
                User user = databaseDriver.getUserById(usernameText).get();

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RestaurantSearch.fxml"));
                Parent root = fxmlLoader.load();

                RestaurantSearchController controller = fxmlLoader.getController();
                controller.initialize(user);

                Stage stage = (Stage) create.getScene().getWindow();
                stage.setTitle("Restaurant Search");
                stage.setScene(new Scene(root));
            }
            else {
                messageLabel.setText("Incorrect username or password.");
            }
        }
        databaseDriver.disconnect();
    }

    @FXML
    public void exit() throws IOException {
        System.exit(0);
    }

}