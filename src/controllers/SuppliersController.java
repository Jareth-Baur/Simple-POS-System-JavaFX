package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.DatabaseConnection;
import utils.Session;

import java.sql.*;

public class SuppliersController {

    @FXML private TableView<Supplier> table;
    @FXML private TableColumn<Supplier, Integer> colId;
    @FXML private TableColumn<Supplier, String> colName;
    @FXML private TableColumn<Supplier, String> colEmail;
    @FXML private TableColumn<Supplier, String> colPhone;
    @FXML private TableColumn<Supplier, String> colAddress;

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;

    private final ObservableList<Supplier> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        table.setItems(list);
        loadSuppliers();

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, s) -> {
            if (s == null) return;
            nameField.setText(s.getName());
            emailField.setText(s.getEmail());
            phoneField.setText(s.getPhone());
            addressField.setText(s.getAddress());
        });

        if (!"admin".equalsIgnoreCase(Session.getRole())) {
            disableCrud();
        }
    }

    /* ===============================
       LOAD
       =============================== */
    private void loadSuppliers() {
        list.clear();

        String sql = "SELECT * FROM suppliers ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")
                ));
            }

        } catch (SQLException e) {
            showError("Failed to load suppliers.");
            e.printStackTrace();
        }
    }

    /* ===============================
       CRUD
       =============================== */

    @FXML
    private void handleAdd() {
        if (!isAdmin()) return;

        if (nameField.getText().isBlank()) {
            showError("Supplier name is required.");
            return;
        }

        String sql = """
            INSERT INTO suppliers (name, email, phone, address)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            fill(ps);
            ps.executeUpdate();
            loadSuppliers();
            handleClear();

        } catch (SQLException e) {
            showError("Failed to add supplier.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        if (!isAdmin()) return;

        Supplier s = table.getSelectionModel().getSelectedItem();
        if (s == null) {
            showError("Select a supplier to update.");
            return;
        }

        String sql = """
            UPDATE suppliers
            SET name=?, email=?, phone=?, address=?
            WHERE id=?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            fill(ps);
            ps.setInt(5, s.getId());
            ps.executeUpdate();
            loadSuppliers();

        } catch (SQLException e) {
            showError("Failed to update supplier.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        if (!isAdmin()) return;

        Supplier s = table.getSelectionModel().getSelectedItem();
        if (s == null) {
            showError("Select a supplier to delete.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement("DELETE FROM suppliers WHERE id=?")) {

            ps.setInt(1, s.getId());
            ps.executeUpdate();
            loadSuppliers();
            handleClear();

        } catch (SQLException e) {
            showError("Supplier is in use by products.");
        }
    }

    @FXML
    private void handleClear() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        table.getSelectionModel().clearSelection();
    }

    /* ===============================
       HELPERS
       =============================== */

    private void fill(PreparedStatement ps) throws SQLException {
        ps.setString(1, nameField.getText());
        ps.setString(2, emailField.getText());
        ps.setString(3, phoneField.getText());
        ps.setString(4, addressField.getText());
    }

    private boolean isAdmin() {
        return "admin".equalsIgnoreCase(Session.getRole());
    }

    private void disableCrud() {
        nameField.setDisable(true);
        emailField.setDisable(true);
        phoneField.setDisable(true);
        addressField.setDisable(true);
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    /* ===============================
       INLINE MODEL
       =============================== */
    public static class Supplier {
        private final int id;
        private final String name;
        private final String email;
        private final String phone;
        private final String address;

        public Supplier(int id, String name, String email, String phone, String address) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.address = address;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
    }
}
