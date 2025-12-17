package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.Main;
import utils.DatabaseConnection;

import java.sql.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

public class RegisterController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private AnchorPane rootPane;

    @FXML
    public void initialize() {
        rootPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleRegister();
            }
        });
    }

    @FXML
    private void handleRegister() {

        if (emailField.getText().isBlank() || passwordField.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "All fields are required.");
            return;
        }

        String sql = "INSERT INTO users (email, password) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, emailField.getText());
            stmt.setString(2, passwordField.getText());
            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Account created successfully!");
            Main.setRoot("login.fxml");

        } catch (SQLIntegrityConstraintViolationException e) {
            showAlert(Alert.AlertType.ERROR, "Email already exists.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    private void switchToLogin() throws Exception {
        Main.setRoot("login.fxml");
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }
}
