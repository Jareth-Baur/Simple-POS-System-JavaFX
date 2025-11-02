package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.DatabaseConnection;
import models.Product;
import models.Sale;
import java.sql.*;

public class SalesController {

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colProdId;
    @FXML private TableColumn<Product, String> colProdName;
    @FXML private TableColumn<Product, Double> colProdPrice;

    @FXML private TableView<Sale> cartTable;
    @FXML private TableColumn<Sale, String> colCartName;
    @FXML private TableColumn<Sale, Integer> colCartQty;
    @FXML private TableColumn<Sale, Double> colCartTotal;

    @FXML private TextField quantityField;
    @FXML private Label totalLabel;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<Sale> cartList = FXCollections.observableArrayList();
    private double totalAmount = 0;

    @FXML
    public void initialize() {
        colProdId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProdName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProdPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        colCartName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        loadProducts();
        productTable.setItems(productList);
        cartTable.setItems(cartList);
    }

    private void loadProducts() {
        productList.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM products WHERE stock > 0";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                productList.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("stock")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddToCart() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a product.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(quantityField.getText());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid quantity.");
            return;
        }

        double total = selected.getPrice() * qty;
        totalAmount += total;

        cartList.add(new Sale(0, selected.getId(), selected.getName(), qty, total));
        updateTotalLabel();
        quantityField.clear();
    }

    @FXML
    private void handleRemove() {
        Sale selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            totalAmount -= selected.getTotal();
            cartList.remove(selected);
            updateTotalLabel();
        }
    }

    @FXML
    private void handleCheckout() {
        if (cartList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cart is empty!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO sales (product_id, quantity, total) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            for (Sale s : cartList) {
                stmt.setInt(1, s.getProductId());
                stmt.setInt(2, s.getQuantity());
                stmt.setDouble(3, s.getTotal());
                stmt.addBatch();

                // Deduct stock
                PreparedStatement updateStock = conn.prepareStatement(
                    "UPDATE products SET stock = stock - ? WHERE id = ?");
                updateStock.setInt(1, s.getQuantity());
                updateStock.setInt(2, s.getProductId());
                updateStock.executeUpdate();
            }
            stmt.executeBatch();

            showAlert(Alert.AlertType.INFORMATION, "Checkout successful!");
            cartList.clear();
            totalAmount = 0;
            updateTotalLabel();
            loadProducts();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    private void updateTotalLabel() {
        totalLabel.setText(String.format("%.2f", totalAmount));
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.showAndWait();
    }
}
