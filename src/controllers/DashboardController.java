package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import java.io.IOException;
import main.Main;

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

    private static String loggedInUser;

    public static void setLoggedInUser(String email) {
        loggedInUser = email;
    }

    @FXML
    public void initialize() {
        if (loggedInUser != null) {
            welcomeLabel.setText("Welcome, " + loggedInUser);
        }
        // Show placeholder at startup
        placeholderLabel.setVisible(true);
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("Your current session will be closed.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                Main.setRoot("login.fxml");
            } catch (Exception e) {
                System.out.println(e.getMessage());
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
            Pane newLoadedPane = FXMLLoader.load(getClass().getResource("/views/" + fxml));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newLoadedPane);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
