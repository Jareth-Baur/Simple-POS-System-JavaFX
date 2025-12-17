package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.Main;
import utils.DatabaseConnection;

import java.sql.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import utils.Session;

public class LoginController {

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
                handleLogin();
            }
        });
    }

    @FXML
    private void handleLogin() {

        if (emailField.getText().isBlank() || passwordField.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "All fields are required.");
            return;
        }

        String sql = """
            SELECT id, email, role
            FROM users
            WHERE email = ? AND password = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, emailField.getText());
            stmt.setString(2, passwordField.getText());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                String email = rs.getString("email");
                String role = rs.getString("role");

                // 🔐 Store full session data
                Session.setUser(userId, email, role);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Login Successful");
                alert.setHeaderText(null);
                alert.setContentText("Welcome, " + email + "!");
                alert.show();
                Main.setRoot("dashboard.fxml");
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid email or password.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    private void switchToRegister() throws Exception {
        Main.setRoot("register.fxml");
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }
}
