package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.DatabaseConnection;
import utils.Session;

import java.sql.*;

public class BundlesController {

    /* TABLE */
    @FXML private TableView<Bundle> table;
    @FXML private TableColumn<Bundle, Integer> colId;
    @FXML private TableColumn<Bundle, String> colBundle;
    @FXML private TableColumn<Bundle, String> colItem;
    @FXML private TableColumn<Bundle, Integer> colQty;

    /* FORM */
    @FXML private ComboBox<ProductItem> bundleBox;
    @FXML private ComboBox<ProductItem> itemBox;
    @FXML private TextField qtyField;

    private final ObservableList<Bundle> list = FXCollections.observableArrayList();
    private final ObservableList<ProductItem> products = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBundle.setCellValueFactory(new PropertyValueFactory<>("bundleName"));
        colItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        table.setItems(list);
        bundleBox.setItems(products);
        itemBox.setItems(products);

        loadProducts();
        loadBundles();

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, b) -> {
            if (b == null) return;

            products.stream().filter(p -> p.getId() == b.getBundleProductId())
                    .findFirst().ifPresent(bundleBox::setValue);

            products.stream().filter(p -> p.getId() == b.getItemProductId())
                    .findFirst().ifPresent(itemBox::setValue);

            qtyField.setText(String.valueOf(b.getQuantity()));
        });

        if (!"admin".equalsIgnoreCase(Session.getRole())) disableCrud();
    }

    /* ===============================
       LOADERS
       =============================== */

    private void loadProducts() {
        products.clear();

        String sql = "SELECT id, name FROM products WHERE is_active=1 ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(new ProductItem(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }

        } catch (SQLException e) {
            error("Failed to load products.");
            e.printStackTrace();
        }
    }

    private void loadBundles() {
        list.clear();

        String sql = """
            SELECT b.id,
                   b.bundle_product_id,
                   bp.name AS bundle_name,
                   b.item_product_id,
                   ip.name AS item_name,
                   b.quantity
            FROM product_bundles b
            JOIN products bp ON b.bundle_product_id = bp.id
            JOIN products ip ON b.item_product_id = ip.id
            ORDER BY bundle_name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Bundle(
                        rs.getInt("id"),
                        rs.getInt("bundle_product_id"),
                        rs.getString("bundle_name"),
                        rs.getInt("item_product_id"),
                        rs.getString("item_name"),
                        rs.getInt("quantity")
                ));
            }

        } catch (SQLException e) {
            error("Failed to load bundles.");
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
            INSERT INTO product_bundles
            (bundle_product_id, item_product_id, quantity)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bundleBox.getValue().getId());
            ps.setInt(2, itemBox.getValue().getId());
            ps.setInt(3, Integer.parseInt(qtyField.getText()));

            ps.executeUpdate();
            loadBundles();
            handleClear();

        } catch (SQLIntegrityConstraintViolationException e) {
            error("This item is already part of the bundle.");
        } catch (SQLException e) {
            error("Failed to add bundle item.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Bundle b = table.getSelectionModel().getSelectedItem();
        if (b == null) {
            error("Select a bundle item to delete.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement("DELETE FROM product_bundles WHERE id=?")) {

            ps.setInt(1, b.getId());
            ps.executeUpdate();
            loadBundles();
            handleClear();

        } catch (SQLException e) {
            error("Failed to delete bundle item.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        bundleBox.setValue(null);
        itemBox.setValue(null);
        qtyField.clear();
        table.getSelectionModel().clearSelection();
    }

    /* ===============================
       HELPERS
       =============================== */

    private boolean validate() {
        if (!"admin".equalsIgnoreCase(Session.getRole())) {
            error("Only admin can manage bundles.");
            return false;
        }

        if (bundleBox.getValue() == null || itemBox.getValue() == null) {
            error("Select both bundle and item products.");
            return false;
        }

        if (bundleBox.getValue().getId() == itemBox.getValue().getId()) {
            error("A product cannot bundle itself.");
            return false;
        }

        try {
            int qty = Integer.parseInt(qtyField.getText());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            error("Quantity must be a positive number.");
            return false;
        }

        return true;
    }

    private void disableCrud() {
        bundleBox.setDisable(true);
        itemBox.setDisable(true);
        qtyField.setDisable(true);
    }

    private void error(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    /* ===============================
       INLINE MODELS
       =============================== */

    public static class Bundle {
        private final int id;
        private final int bundleProductId;
        private final String bundleName;
        private final int itemProductId;
        private final String itemName;
        private final int quantity;

        public Bundle(int id, int bundleProductId, String bundleName,
                      int itemProductId, String itemName, int quantity) {
            this.id = id;
            this.bundleProductId = bundleProductId;
            this.bundleName = bundleName;
            this.itemProductId = itemProductId;
            this.itemName = itemName;
            this.quantity = quantity;
        }

        public int getId() { return id; }
        public int getBundleProductId() { return bundleProductId; }
        public String getBundleName() { return bundleName; }
        public int getItemProductId() { return itemProductId; }
        public String getItemName() { return itemName; }
        public int getQuantity() { return quantity; }
    }

    public static class ProductItem {
        private final int id;
        private final String name;

        public ProductItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }

        @Override
        public String toString() {
            return name;
        }
    }
}
