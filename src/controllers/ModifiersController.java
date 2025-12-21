package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.DatabaseConnection;
import utils.Session;

import java.sql.*;
import javafx.fxml.FXMLLoader;

public class ModifiersController {

    @FXML
    private TableView<Modifier> table;
    @FXML
    private TableColumn<Modifier, Integer> colId;
    @FXML
    private TableColumn<Modifier, String> colProduct;
    @FXML
    private TableColumn<Modifier, String> colName;
    @FXML
    private TableColumn<Modifier, String> colType;
    @FXML
    private TableColumn<Modifier, Boolean> colRequired;

    @FXML
    private ComboBox<ProductItem> productBox;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> typeBox;
    @FXML
    private CheckBox requiredBox;

    private final ObservableList<Modifier> list = FXCollections.observableArrayList();
    private final ObservableList<ProductItem> products = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colRequired.setCellValueFactory(new PropertyValueFactory<>("required"));

        typeBox.setItems(FXCollections.observableArrayList("single", "multiple"));

        table.setItems(list);
        productBox.setItems(products);

        // ✅ FIX HERE
        typeBox.setItems(FXCollections.observableArrayList("single", "multiple"));

        loadProducts();
        loadModifiers();

        table.getSelectionModel().selectedItemProperty().addListener((o, old, m) -> {
            if (m == null) {
                return;
            }
            nameField.setText(m.getName());
            typeBox.setValue(m.getType());
            requiredBox.setSelected(m.isRequired());
            products.stream()
                    .filter(p -> p.getId() == m.getProductId())
                    .findFirst().ifPresent(productBox::setValue);
        });

        if (!"admin".equalsIgnoreCase(Session.getRole())) {
            disableCrud();
        }
    }

    private void loadProducts() {
        products.clear();
        try (Connection c = DatabaseConnection.getConnection(); ResultSet rs = c.createStatement()
                .executeQuery("SELECT id, name FROM products WHERE is_active=1")) {
            while (rs.next()) {
                products.add(new ProductItem(rs.getInt("id"), rs.getString("name")));
            }
        } catch (Exception e) {
            error("Failed to load products");
        }
    }

    private void loadModifiers() {
        list.clear();
        String sql = """
            SELECT m.*, p.name AS product_name
            FROM product_modifiers m
            JOIN products p ON m.product_id = p.id
        """;
        try (Connection c = DatabaseConnection.getConnection(); ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Modifier(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getBoolean("required")
                ));
            }
        } catch (Exception e) {
            error("Failed to load modifiers");
        }
    }

    @FXML
    private void handleAdd() {
        if (!validate()) {
            return;
        }

        String sql = """
            INSERT INTO product_modifiers(product_id, name, type, required)
            VALUES (?, ?, ?, ?)
        """;
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            fill(ps);
            ps.executeUpdate();
            loadModifiers();
            handleClear();
        } catch (Exception e) {
            error("Failed to add modifier");
        }
    }

    @FXML
    private void handleUpdate() {
        Modifier m = table.getSelectionModel().getSelectedItem();
        if (m == null || !validate()) {
            return;
        }

        String sql = """
            UPDATE product_modifiers
            SET product_id=?, name=?, type=?, required=?
            WHERE id=?
        """;
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            fill(ps);
            ps.setInt(5, m.getId());
            ps.executeUpdate();
            loadModifiers();
        } catch (Exception e) {
            error("Failed to update modifier");
        }
    }

    @FXML
    private void handleDelete() {
        Modifier m = table.getSelectionModel().getSelectedItem();
        if (m == null) {
            return;
        }

        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps
                = c.prepareStatement("DELETE FROM product_modifiers WHERE id=?")) {
            ps.setInt(1, m.getId());
            ps.executeUpdate();
            loadModifiers();
            handleClear();
        } catch (Exception e) {
            error("Modifier has options.");
        }
    }

    @FXML
    private void handleClear() {
        nameField.clear();
        typeBox.setValue(null);
        requiredBox.setSelected(false);
        productBox.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    @FXML
    private void openOptions() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/modifier_options.fxml")
            );
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Modifier Options");
            dialog.getDialogPane().setContent(loader.load());
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to open modifier options.").showAndWait();
            e.printStackTrace();
        }
    }

    private boolean validate() {
        if (!"admin".equalsIgnoreCase(Session.getRole())) {
            return false;
        }
        if (productBox.getValue() == null || nameField.getText().isBlank()) {
            error("Product and modifier name required");
            return false;
        }
        return true;
    }

    private void fill(PreparedStatement ps) throws SQLException {
        ps.setInt(1, productBox.getValue().getId());
        ps.setString(2, nameField.getText());
        ps.setString(3, typeBox.getValue());
        ps.setBoolean(4, requiredBox.isSelected());
    }

    private void disableCrud() {
        productBox.setDisable(true);
        nameField.setDisable(true);
        typeBox.setDisable(true);
        requiredBox.setDisable(true);
    }

    private void error(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    /* ===== Inline Models ===== */
    public static class Modifier {

        private final int id, productId;
        private final String productName, name, type;
        private final boolean required;

        public Modifier(int id, int productId, String productName,
                String name, String type, boolean required) {
            this.id = id;
            this.productId = productId;
            this.productName = productName;
            this.name = name;
            this.type = type;
            this.required = required;
        }

        public int getId() {
            return id;
        }

        public int getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public boolean isRequired() {
            return required;
        }
    }

    public static class ProductItem {

        private final int id;
        private final String name;

        public ProductItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }
    }
}
