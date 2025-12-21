package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import main.Main;
import utils.Session;

import java.io.IOException;

public class DashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label placeholderLabel;

    @FXML
    private Button btnProducts;
    @FXML
    private Button btnSales;
    @FXML
    private Button btnReports;
    @FXML
    private Button btnLogout;

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {

        String email = Session.getEmail();
        String role = Session.getRole();

        // Safety check (prevents null crashes)
        if (email == null || role == null) {
            forceLogout();
            return;
        }

        welcomeLabel.setText("Welcome, " + email);
        placeholderLabel.setVisible(true);

        // 🔐 ROLE-BASED UI CONTROL (PDF-COMPLIANT)
        if ("cashier".equalsIgnoreCase(role)) {

            // Cashier rules
            btnProducts.setDisable(true);
            btnReports.setDisable(true);

            // Optional: guide cashier
            placeholderLabel.setText("Proceed to Sales to start a transaction.");

        } else if ("admin".equalsIgnoreCase(role)) {

            // Admin has full access
            btnProducts.setDisable(false);
            btnSales.setDisable(false);
            btnReports.setDisable(false);

        } else {
            // Unknown role = deny access
            forceLogout();
        }
    }

    // ===============================
    // Navigation Handlers
    // ===============================
    @FXML
private void handleProducts() {
    loadPage("products_main.fxml");
    setActive(btnProducts);
}


    @FXML
private void handleSales() {
    loadPage("sales.fxml");
    setActive(btnSales);
}


    @FXML
private void handleReports() {
    loadPage("reports.fxml");
    setActive(btnReports);
}


    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("Your current session will be closed.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            forceLogout();
        }
    }

    // ===============================
    // Helpers
    // ===============================
    private void loadPage(String fxml) {
        try {
            Pane page = FXMLLoader.load(
                    getClass().getResource("/views/" + fxml)
            );
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            showError("Failed to load page.");
            e.printStackTrace();
        }
    }

    private void forceLogout() {
        Session.clear();
        try {
            Main.setRoot("login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void setActive(Button activeBtn) {
        btnProducts.getStyleClass().remove("active");
        btnSales.getStyleClass().remove("active");
        btnReports.getStyleClass().remove("active");

        activeBtn.getStyleClass().add("active");
    }

}
