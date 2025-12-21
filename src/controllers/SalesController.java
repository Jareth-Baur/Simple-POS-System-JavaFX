package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.DatabaseConnection;
import utils.Session;

import java.sql.*;

public class SalesController {

    /* ===============================
       PRODUCT TABLE
       =============================== */
    @FXML private TableView<POSProduct> productTable;
    @FXML private TableColumn<POSProduct, Integer> colProdId;
    @FXML private TableColumn<POSProduct, String> colProdName;
    @FXML private TableColumn<POSProduct, Double> colProdPrice;

    /* ===============================
       CART TABLE
       =============================== */
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colCartName;
    @FXML private TableColumn<CartItem, Integer> colCartQty;
    @FXML private TableColumn<CartItem, Double> colCartSubtotal;

    @FXML private TextField quantityField;
    @FXML private Label totalLabel;

    private final ObservableList<POSProduct> productList = FXCollections.observableArrayList();
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

        productTable.setItems(productList);
        cartTable.setItems(cartList);

        loadProducts();
        updateTotal();
    }

    /* ===============================
       LOAD PRODUCTS
       =============================== */
    private void loadProducts() {
        productList.clear();

        String sql = """
            SELECT id, name, price, stock
            FROM products
            WHERE is_active = 1 AND stock > 0
            ORDER BY name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productList.add(new POSProduct(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                ));
            }

        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Failed to load products.");
        }
    }

    /* ===============================
       ADD TO CART (MERGED LOGIC)
       =============================== */
    @FXML
    private void handleAddToCart() {

        POSProduct p = productTable.getSelectionModel().getSelectedItem();
        if (p == null) {
            alert(Alert.AlertType.WARNING, "Select a product.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(quantityField.getText());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            alert(Alert.AlertType.ERROR, "Invalid quantity.");
            return;
        }

        // 🔐 Prevent overselling
        int alreadyInCart = cartList.stream()
                .filter(c -> c.getProductId() == p.getId())
                .mapToInt(CartItem::getQuantity)
                .sum();

        if (qty + alreadyInCart > p.getStock()) {
            alert(Alert.AlertType.ERROR, "Insufficient stock.");
            return;
        }

        // 🔁 Merge if exists
        for (CartItem c : cartList) {
            if (c.getProductId() == p.getId()) {
                c.addQuantity(qty);
                updateTotal();
                quantityField.clear();
                cartTable.refresh();
                return;
            }
        }

        cartList.add(new CartItem(p.getId(), p.getName(), qty, p.getPrice()));
        updateTotal();
        quantityField.clear();
    }

    /* ===============================
       REMOVE FROM CART
       =============================== */
    @FXML
    private void handleRemove() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        cartList.remove(selected);
        updateTotal();
    }

    /* ===============================
       CHECKOUT (TRANSACTION)
       =============================== */
    @FXML
    private void handleCheckout() {

        if (cartList.isEmpty()) {
            alert(Alert.AlertType.WARNING, "Cart is empty.");
            return;
        }

        String insertSale = "INSERT INTO sales (user_id, total_amount) VALUES (?, ?)";
        String insertItem = """
            INSERT INTO sale_items (sale_id, product_id, quantity, price, subtotal)
            VALUES (?, ?, ?, ?, ?)
        """;
        String updateStock = "UPDATE products SET stock = stock - ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            // 1️⃣ Create sale
            PreparedStatement saleStmt =
                    conn.prepareStatement(insertSale, Statement.RETURN_GENERATED_KEYS);
            saleStmt.setInt(1, Session.getUserId());
            saleStmt.setDouble(2, totalAmount);
            saleStmt.executeUpdate();

            ResultSet keys = saleStmt.getGeneratedKeys();
            if (!keys.next()) throw new SQLException("Sale creation failed.");
            int saleId = keys.getInt(1);

            PreparedStatement itemStmt = conn.prepareStatement(insertItem);
            PreparedStatement stockStmt = conn.prepareStatement(updateStock);

            for (CartItem c : cartList) {

                itemStmt.setInt(1, saleId);
                itemStmt.setInt(2, c.getProductId());
                itemStmt.setInt(3, c.getQuantity());
                itemStmt.setDouble(4, c.getPrice());
                itemStmt.setDouble(5, c.getSubtotal());
                itemStmt.addBatch();

                stockStmt.setInt(1, c.getQuantity());
                stockStmt.setInt(2, c.getProductId());
                stockStmt.addBatch();
            }

            itemStmt.executeBatch();
            stockStmt.executeBatch();
            conn.commit();

            alert(Alert.AlertType.INFORMATION, "Checkout successful!");

            cartList.clear();
            totalAmount = 0;
            updateTotal();
            loadProducts();

        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Checkout failed.");
            e.printStackTrace();
        }
    }

    /* ===============================
       HELPERS
       =============================== */
    private void updateTotal() {
        totalAmount = cartList.stream().mapToDouble(CartItem::getSubtotal).sum();
        totalLabel.setText(String.format("%.2f", totalAmount));
    }

    private void alert(Alert.AlertType type, String msg) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }

    /* ===============================
       INLINE MODELS
       =============================== */

    public static class POSProduct {
        private final int id, stock;
        private final String name;
        private final double price;

        public POSProduct(int id, String name, double price, int stock) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getStock() { return stock; }
    }

    public static class CartItem {
        private final int productId;
        private final String productName;
        private int quantity;
        private final double price;

        public CartItem(int productId, String productName, int quantity, double price) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }

        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getSubtotal() { return quantity * price; }

        public void addQuantity(int qty) {
            this.quantity += qty;
        }
    }
}
