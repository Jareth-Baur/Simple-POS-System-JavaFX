package controllers;

import main.Main;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.DatabaseConnection;
import java.sql.*;
import javafx.event.ActionEvent;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    public void handleRegister() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please fill in all fields.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO users (email, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Account created successfully!");
            Main.setRoot("login.fxml");
        } catch (SQLIntegrityConstraintViolationException e) {
            showAlert(Alert.AlertType.ERROR, "Email already registered!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }

    @FXML
    public void switchToLogin(ActionEvent event) {
        try {
            Main.setRoot("login.fxml");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.showAndWait();
    }
}
