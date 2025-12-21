package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.DatabaseConnection;
import utils.Session;

import java.sql.*;

public class VariantsController {

    /* TABLE */
    @FXML private TableView<Variant> table;
    @FXML private TableColumn<Variant, Integer> colId;
    @FXML private TableColumn<Variant, String> colProduct;
    @FXML private TableColumn<Variant, String> colName;
    @FXML private TableColumn<Variant, String> colSku;
    @FXML private TableColumn<Variant, String> colBarcode;
    @FXML private TableColumn<Variant, Double> colPrice;
    @FXML private TableColumn<Variant, Integer> colStock;

    /* FORM */
    @FXML private ComboBox<ProductItem> productBox;
    @FXML private TextField nameField;
    @FXML private TextField skuField;
    @FXML private TextField barcodeField;
    @FXML private TextField priceField;
    @FXML private TextField stockField;

    private final ObservableList<Variant> variantList = FXCollections.observableArrayList();
    private final ObservableList<ProductItem> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSku.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        table.setItems(variantList);
        productBox.setItems(productList);

        loadProducts();
        loadVariants();

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, v) -> {
            if (v == null) return;

            nameField.setText(v.getName());
            skuField.setText(v.getSku());
            barcodeField.setText(v.getBarcode());
            priceField.setText(String.valueOf(v.getPrice()));
            stockField.setText(String.valueOf(v.getStock()));

            productBox.getItems().stream()
                    .filter(p -> p.getId() == v.getProductId())
                    .findFirst()
                    .ifPresent(productBox::setValue);
        });

        if (!"admin".equalsIgnoreCase(Session.getRole())) {
            disableCrud();
        }
    }

    /* ===============================
       LOADERS
       =============================== */

    private void loadProducts() {
        productList.clear();

        String sql = "SELECT id, name FROM products WHERE is_active=1 ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productList.add(new ProductItem(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }

        } catch (SQLException e) {
            showError("Failed to load products.");
            e.printStackTrace();
        }
    }

    private void loadVariants() {
        variantList.clear();

        String sql = """
            SELECT v.*, p.name AS product_name
            FROM product_variants v
            JOIN products p ON v.product_id = p.id
            ORDER BY p.name, v.name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                variantList.add(new Variant(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("name"),
                        rs.getString("sku"),
                        rs.getString("barcode"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                ));
            }

        } catch (SQLException e) {
            showError("Failed to load variants.");
            e.printStackTrace();
        }
    }

    /* ===============================
       CRUD
       =============================== */

    @FXML
    private void handleAdd() {
        if (!validate()) return;

        String sql = """
            INSERT INTO product_variants
            (product_id, name, sku, barcode, price, stock)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            fill(ps);
            ps.executeUpdate();
            loadVariants();
            handleClear();

        } catch (SQLIntegrityConstraintViolationException e) {
            showError("SKU or Barcode already exists.");
        } catch (SQLException e) {
            showError("Failed to add variant.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        Variant v = table.getSelectionModel().getSelectedItem();
        if (v == null) {
            showError("Select a variant to update.");
            return;
        }

        if (!validate()) return;

        String sql = """
            UPDATE product_variants SET
                product_id=?, name=?, sku=?, barcode=?, price=?, stock=?
            WHERE id=?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            fill(ps);
            ps.setInt(7, v.getId());
            ps.executeUpdate();
            loadVariants();

        } catch (SQLException e) {
            showError("Failed to update variant.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Variant v = table.getSelectionModel().getSelectedItem();
        if (v == null) {
            showError("Select a variant to delete.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement("DELETE FROM product_variants WHERE id=?")) {

            ps.setInt(1, v.getId());
            ps.executeUpdate();
            loadVariants();
            handleClear();

        } catch (SQLException e) {
            showError("Failed to delete variant.");
        }
    }

    /* ===============================
       HELPERS
       =============================== */

    private boolean validate() {
        if (!"admin".equalsIgnoreCase(Session.getRole())) {
            showError("Only admin can modify variants.");
            return false;
        }

        if (productBox.getValue() == null || nameField.getText().isBlank()) {
            showError("Product and Variant Name are required.");
            return false;
        }

        try {
            Double.parseDouble(priceField.getText());
            Integer.parseInt(stockField.getText());
        } catch (NumberFormatException e) {
            showError("Price must be a number. Stock must be an integer.");
            return false;
        }

        return true;
    }

    private void fill(PreparedStatement ps) throws SQLException {
        ps.setInt(1, productBox.getValue().getId());
        ps.setString(2, nameField.getText());
        ps.setString(3, skuField.getText());
        ps.setString(4, barcodeField.getText());
        ps.setDouble(5, Double.parseDouble(priceField.getText()));
        ps.setInt(6, Integer.parseInt(stockField.getText()));
    }

    @FXML
    private void handleClear() {
        nameField.clear();
        skuField.clear();
        barcodeField.clear();
        priceField.clear();
        stockField.clear();
        productBox.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    private void disableCrud() {
        productBox.setDisable(true);
        nameField.setDisable(true);
        skuField.setDisable(true);
        barcodeField.setDisable(true);
        priceField.setDisable(true);
        stockField.setDisable(true);
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    /* ===============================
       INLINE MODELS
       =============================== */

    public static class Variant {
        private final int id;
        private final int productId;
        private final String productName;
        private final String name;
        private final String sku;
        private final String barcode;
        private final double price;
        private final int stock;

        public Variant(int id, int productId, String productName,
                       String name, String sku, String barcode,
                       double price, int stock) {
            this.id = id;
            this.productId = productId;
            this.productName = productName;
            this.name = name;
            this.sku = sku;
            this.barcode = barcode;
            this.price = price;
            this.stock = stock;
        }

        public int getId() { return id; }
        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getName() { return name; }
        public String getSku() { return sku; }
        public String getBarcode() { return barcode; }
        public double getPrice() { return price; }
        public int getStock() { return stock; }
    }

    public static class ProductItem {
        private final int id;
        private final String name;

        public ProductItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return name; // shown in ComboBox
        }
    }
}
