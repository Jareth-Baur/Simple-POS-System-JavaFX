package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class ProductsMainController {

    @FXML private StackPane contentArea;
    @FXML private HBox subNav;

    @FXML private Button btnCategories;
    @FXML private Button btnSuppliers;
    @FXML private Button btnProducts;
    @FXML private Button btnVariants;
    @FXML private Button btnModifiers;
    @FXML private Button btnBundles;

    @FXML
    public void initialize() {
        openProducts(); // default tab
    }

    /* ========= BUTTON ACTIONS ========= */

    @FXML
    private void openCategories() {
        loadView("categories.fxml");
        setActive(btnCategories);
    }

    @FXML
    private void openSuppliers() {
        loadView("suppliers.fxml");
        setActive(btnSuppliers);
    }

    @FXML
    private void openProducts() {
        loadView("products.fxml");
        setActive(btnProducts);
    }

    @FXML
    private void openVariants() {
        loadView("variants.fxml");
        setActive(btnVariants);
    }

    @FXML
    private void openModifiers() {
        loadView("modifiers.fxml");
        setActive(btnModifiers);
    }

    @FXML
    private void openBundles() {
        loadView("bundles.fxml");
        setActive(btnBundles);
    }

    /* ========= HELPERS ========= */

    private void loadView(String fxml) {
        try {
            Pane view = FXMLLoader.load(
                getClass().getResource("/views/" + fxml)
            );
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActive(Button activeBtn) {
        for (Node node : subNav.getChildren()) {
            node.getStyleClass().remove("active");
        }
        activeBtn.getStyleClass().add("active");
    }
}
