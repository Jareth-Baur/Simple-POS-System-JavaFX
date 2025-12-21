package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import main.Main;
import utils.DatabaseConnection;
import utils.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private AnchorPane rootPane;

    @FXML
    private void handleLogin() {

        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "All fields are required.");
            return;
        }

        String sql = """
            SELECT id, email, role
            FROM users
            WHERE email = ? AND password = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Invalid email or password.");
                return;
            }

            int userId = rs.getInt("id");
            String role = rs.getString("role");

            // Store session (Lab #3 requirement)
            Session.setUser(userId, email, role);

            showAlert(Alert.AlertType.INFORMATION, "Login successful!");

            // Role-based redirect (PDF requirement)
            if ("cashier".equalsIgnoreCase(role)) {
                Main.setRoot("dashboard.fxml");
            } else {
                Main.setRoot("dashboard.fxml");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database error. Please try again.");
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected error occurred.");
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToRegister() throws Exception {
        Main.setRoot("register.fxml");
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
