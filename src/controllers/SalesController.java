package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;

import models.Product;
import models.CartItem;
import utils.DatabaseConnection;
import utils.Session;

import java.sql.*;

public class SalesController {

    /* Product table */
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Integer> colProdId;
    @FXML
    private TableColumn<Product, String> colProdName;
    @FXML
    private TableColumn<Product, Double> colProdPrice;

    /* Cart table */
    @FXML
    private TableView<CartItem> cartTable;
    @FXML
    private TableColumn<CartItem, String> colCartName;
    @FXML
    private TableColumn<CartItem, Integer> colCartQty;
    @FXML
    private TableColumn<CartItem, Double> colCartSubtotal;

    @FXML
    private TextField quantityField;
    @FXML
    private Label totalLabel;

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<CartItem> cartList = FXCollections.observableArrayList();

    private double totalAmount = 0;

    @FXML
    public void initialize() {

        colProdId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProdName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProdPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        colCartName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        loadProducts();

        productTable.setItems(productList);
        cartTable.setItems(cartList);
    }

    private void loadProducts() {
        productList.clear();

        String sql = "SELECT id, name, price, stock FROM products WHERE stock > 0 AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

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
            showAlert(Alert.AlertType.WARNING, "Select a product.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(quantityField.getText());
            if (qty <= 0 || qty > selected.getStock()) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid quantity.");
            return;
        }

        CartItem item = new CartItem(
                selected.getId(),
                selected.getName(),
                qty,
                selected.getPrice()
        );

        cartList.add(item);
        totalAmount += item.getSubtotal();
        updateTotal();

        quantityField.clear();
    }

    @FXML
    private void handleRemove() {

        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        totalAmount -= selected.getSubtotal();
        cartList.remove(selected);
        updateTotal();
    }

    @FXML
    private void handleCheckout() {

        if (cartList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cart is empty.");
            return;
        }

        String insertSale
                = "INSERT INTO sales (user_id, total_amount) VALUES (?, ?)";

        String insertItem
                = "INSERT INTO sale_items (sale_id, product_id, quantity, price, subtotal) "
                + "VALUES (?, ?, ?, ?, ?)";

        String updateStock
                = "UPDATE products SET stock = stock - ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            // 1️⃣ Create sale
            PreparedStatement saleStmt
                    = conn.prepareStatement(insertSale, Statement.RETURN_GENERATED_KEYS);
            saleStmt.setInt(1, Session.getUserId());
            saleStmt.setDouble(2, totalAmount);
            saleStmt.executeUpdate();

            ResultSet keys = saleStmt.getGeneratedKeys();
            if (!keys.next()) {
                throw new SQLException("Failed to create sale.");
            }
            int saleId = keys.getInt(1);

            // 2️⃣ Insert sale items + deduct stock
            PreparedStatement itemStmt = conn.prepareStatement(insertItem);
            PreparedStatement stockStmt = conn.prepareStatement(updateStock);

            for (CartItem item : cartList) {

                itemStmt.setInt(1, saleId);
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setDouble(4, item.getPrice());
                itemStmt.setDouble(5, item.getSubtotal());
                itemStmt.addBatch();

                stockStmt.setInt(1, item.getQuantity());
                stockStmt.setInt(2, item.getProductId());
                stockStmt.addBatch();
            }

            itemStmt.executeBatch();
            stockStmt.executeBatch();

            conn.commit();

            showAlert(Alert.AlertType.INFORMATION, "Checkout successful!");

            cartList.clear();
            totalAmount = 0;
            updateTotal();
            loadProducts();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    private void updateTotal() {
        totalLabel.setText(String.format("%.2f", totalAmount));
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }
}
