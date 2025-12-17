package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import main.Main;
import utils.Session;

import java.io.IOException;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label placeholderLabel;

    @FXML private Button btnProducts;
    @FXML private Button btnSales;
    @FXML private Button btnReports;
    @FXML private Button btnLogout;

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        // Show logged-in user
        if (Session.getEmail() != null) {
            welcomeLabel.setText("Welcome, " + Session.getEmail());
        }

        placeholderLabel.setVisible(true);

        // Role-based access (DB-driven)
        if (!"admin".equals(Session.getRole())) {
            btnReports.setDisable(true);
        }
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("Your current session will be closed.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Session.clear(); // IMPORTANT
            try {
                Main.setRoot("login.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleProducts() {
        loadPage("products.fxml");
    }

    @FXML
    private void handleSales() {
        loadPage("sales.fxml");
    }

    @FXML
    private void handleReports() {
        loadPage("reports.fxml");
    }

    private void loadPage(String fxml) {
        try {
            Pane page = FXMLLoader.load(
                    getClass().getResource("/views/" + fxml)
            );
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
