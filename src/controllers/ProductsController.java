package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.DatabaseConnection;
import utils.Session;
import models.Product;

import java.sql.*;

public class ProductsController {

    /* TABLE */
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, String> colSku;
    @FXML private TableColumn<Product, String> colBarcode;
    @FXML private TableColumn<Product, Double> colCost;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;

    /* FORM */
    @FXML private TextField nameField;
    @FXML private TextField skuField;
    @FXML private TextField barcodeField;
    @FXML private TextField costField;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private ComboBox<String> categoryBox;

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<String> categoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colSku.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        loadCategories();
        loadProducts();

        productTable.setItems(productList);
        categoryBox.setItems(categoryList);

        productTable.setOnMouseClicked(e -> {
            Product p = productTable.getSelectionModel().getSelectedItem();
            if (p != null) {
                nameField.setText(p.getName());
                skuField.setText(p.getSku());
                barcodeField.setText(p.getBarcode());
                costField.setText(String.valueOf(p.getCost()));
                priceField.setText(String.valueOf(p.getPrice()));
                stockField.setText(String.valueOf(p.getStock()));
                categoryBox.setValue(p.getCategory());
            }
        });

        if (!"admin".equals(Session.getRole())) {
            disableCrud();
        }
    }

    private void loadCategories() {
        categoryList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             ResultSet rs = conn.createStatement()
                     .executeQuery("SELECT name FROM categories ORDER BY name")) {
            while (rs.next()) categoryList.add(rs.getString("name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProducts() {
        productList.clear();

        String sql = """
            SELECT p.id, p.name, p.sku, p.barcode, p.cost, p.price, p.stock,
                   c.name AS category
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.is_active = 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                productList.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("sku"),
                        rs.getString("barcode"),
                        rs.getDouble("cost"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {

        if (!"admin".equals(Session.getRole())) return;

        if (nameField.getText().isBlank()
                || skuField.getText().isBlank()
                || barcodeField.getText().isBlank()
                || costField.getText().isBlank()
                || priceField.getText().isBlank()
                || stockField.getText().isBlank()
                || categoryBox.getValue() == null) {

            showAlert(Alert.AlertType.ERROR, "Please fill in all fields.");
            return;
        }

        String sql = """
            INSERT INTO products
            (name, sku, barcode, cost, price, stock, category_id)
            VALUES (?, ?, ?, ?, ?, ?, (SELECT id FROM categories WHERE name = ?))
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nameField.getText());
            stmt.setString(2, skuField.getText());
            stmt.setString(3, barcodeField.getText());
            stmt.setDouble(4, Double.parseDouble(costField.getText()));
            stmt.setDouble(5, Double.parseDouble(priceField.getText()));
            stmt.setInt(6, Integer.parseInt(stockField.getText()));
            stmt.setString(7, categoryBox.getValue());

            stmt.executeUpdate();
            loadProducts();
            handleClear();

        } catch (SQLIntegrityConstraintViolationException e) {
            showAlert(Alert.AlertType.ERROR, "SKU or Barcode already exists.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {

        if (!"admin".equals(Session.getRole())) return;

        Product p = productTable.getSelectionModel().getSelectedItem();
        if (p == null) return;

        String sql = """
            UPDATE products SET
                name=?, sku=?, barcode=?, cost=?, price=?, stock=?,
                category_id=(SELECT id FROM categories WHERE name=?)
            WHERE id=?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nameField.getText());
            stmt.setString(2, skuField.getText());
            stmt.setString(3, barcodeField.getText());
            stmt.setDouble(4, Double.parseDouble(costField.getText()));
            stmt.setDouble(5, Double.parseDouble(priceField.getText()));
            stmt.setInt(6, Integer.parseInt(stockField.getText()));
            stmt.setString(7, categoryBox.getValue());
            stmt.setInt(8, p.getId());

            stmt.executeUpdate();
            loadProducts();
            handleClear();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {

        if (!"admin".equals(Session.getRole())) return;

        Product p = productTable.getSelectionModel().getSelectedItem();
        if (p == null) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement("UPDATE products SET is_active=0 WHERE id=?")) {

            stmt.setInt(1, p.getId());
            stmt.executeUpdate();
            loadProducts();
            handleClear();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        nameField.clear();
        skuField.clear();
        barcodeField.clear();
        costField.clear();
        priceField.clear();
        stockField.clear();
        categoryBox.setValue(null);
        productTable.getSelectionModel().clearSelection();
    }

    private void disableCrud() {
        nameField.setDisable(true);
        skuField.setDisable(true);
        barcodeField.setDisable(true);
        costField.setDisable(true);
        priceField.setDisable(true);
        stockField.setDisable(true);
        categoryBox.setDisable(true);
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }
}
