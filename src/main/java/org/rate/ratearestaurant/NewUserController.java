package org.rate.ratearestaurant;

import java.io.IOException;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewUserController {
    private DatabaseDriver databaseDriver;
    @FXML
    private Label messageLabel;
    @FXML
    private TextField Username;
    @FXML
    private PasswordField Password;
    @FXML
    private Button back;

    public void initialize() throws SQLException {
        databaseDriver = new DatabaseDriver("appTable.sqlite");
    }

    @FXML
    public void addUser() throws SQLException, IOException {
        databaseDriver.connect();

        String user = Username.getText();
        String pass = Password.getText();

        messageLabel.setText("");

        if(databaseDriver.getUserById(user).isEmpty()) {
            if(pass.length() >= 8) {
                User newUser = new User(user, pass);
                databaseDriver.addUser(newUser);
                databaseDriver.commit();
                messageLabel.setText("Account created successfully! Please return to login screen");
            }
            else {
                messageLabel.setText("Password must be at least 8 characters");
            }
        }
        else {
            messageLabel.setText("Username already taken");
        }
        databaseDriver.disconnect();
    }

    @FXML
    public void backToLogin() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Stage stage = (Stage) back.getScene().getWindow();
        stage.setTitle("Login");
        stage.setScene(new Scene(fxmlLoader.load()));
    }

}