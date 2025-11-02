package controllers;

import main.Main;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.DatabaseConnection;
import java.sql.*;
import javafx.event.ActionEvent;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    @FXML
    public void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please fill in both fields!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String emailLogged = rs.getString("email");
                showAlert(Alert.AlertType.INFORMATION, "Login Successful! Welcome, " + email);
                // Set logged-in user and switch to dashboard
                DashboardController.setLoggedInUser(emailLogged);
                Main.setRoot("dashboard.fxml");
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid credentials!");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }

    @FXML
    public void switchToRegister(ActionEvent event) {
        try {
            Main.setRoot("register.fxml");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.showAndWait();
    }
}
