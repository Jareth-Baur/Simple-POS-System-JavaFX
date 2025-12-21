package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import main.Main;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private AnchorPane rootPane;

    @FXML
    private void handleRegister() {

        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "All fields are required.");
            return;
        }

        String sql = """
            INSERT INTO users (email, password, role)
            VALUES (?, ?, 'cashier')
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);
            ps.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Account created successfully!");
            Main.setRoot("login.fxml");

        } catch (SQLIntegrityConstraintViolationException e) {
            showAlert(Alert.AlertType.ERROR, "Email already exists.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database error. Please try again.");
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected error occurred.");
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToLogin() throws Exception {
        Main.setRoot("login.fxml");
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
