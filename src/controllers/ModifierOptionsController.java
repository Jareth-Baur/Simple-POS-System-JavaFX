package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.DatabaseConnection;
import utils.Session;

import java.sql.*;

public class ModifierOptionsController {

    @FXML private TableView<Option> table;
    @FXML private TableColumn<Option, Integer> colId;
    @FXML private TableColumn<Option, String> colModifier;
    @FXML private TableColumn<Option, String> colName;
    @FXML private TableColumn<Option, Double> colPrice;

    @FXML private ComboBox<ModifierItem> modifierBox;
    @FXML private TextField nameField;
    @FXML private TextField priceField;

    private final ObservableList<Option> list = FXCollections.observableArrayList();
    private final ObservableList<ModifierItem> modifiers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colModifier.setCellValueFactory(new PropertyValueFactory<>("modifierName"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        table.setItems(list);
        modifierBox.setItems(modifiers);

        loadModifiers();
        loadOptions();

        if (!"admin".equalsIgnoreCase(Session.getRole())) disableCrud();
    }

    private void loadModifiers() {
        modifiers.clear();
        try (Connection c = DatabaseConnection.getConnection();
             ResultSet rs = c.createStatement()
                     .executeQuery("SELECT id, name FROM product_modifiers")) {
            while (rs.next())
                modifiers.add(new ModifierItem(rs.getInt("id"), rs.getString("name")));
        } catch (Exception e) { error("Failed to load modifiers"); }
    }

    private void loadOptions() {
        list.clear();
        String sql = """
            SELECT o.*, m.name AS modifier_name
            FROM product_modifier_options o
            JOIN product_modifiers m ON o.modifier_id = m.id
        """;
        try (Connection c = DatabaseConnection.getConnection();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Option(
                        rs.getInt("id"),
                        rs.getInt("modifier_id"),
                        rs.getString("modifier_name"),
                        rs.getString("name"),
                        rs.getDouble("price")
                ));
            }
        } catch (Exception e) { error("Failed to load options"); }
    }

    @FXML
    private void handleAdd() {
        if (!validate()) return;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     c.prepareStatement(
                             "INSERT INTO product_modifier_options(modifier_id, name, price) VALUES (?, ?, ?)")) {
            fill(ps);
            ps.executeUpdate();
            loadOptions();
            handleClear();
        } catch (Exception e) { error("Failed to add option"); }
    }

    @FXML
    private void handleUpdate() {
        Option o = table.getSelectionModel().getSelectedItem();
        if (o == null || !validate()) return;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     c.prepareStatement(
                             "UPDATE product_modifier_options SET modifier_id=?, name=?, price=? WHERE id=?")) {
            fill(ps);
            ps.setInt(4, o.getId());
            ps.executeUpdate();
            loadOptions();
        } catch (Exception e) { error("Failed to update option"); }
    }

    @FXML
    private void handleDelete() {
        Option o = table.getSelectionModel().getSelectedItem();
        if (o == null) return;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     c.prepareStatement("DELETE FROM product_modifier_options WHERE id=?")) {
            ps.setInt(1, o.getId());
            ps.executeUpdate();
            loadOptions();
            handleClear();
        } catch (Exception e) { error("Failed to delete option"); }
    }

    @FXML
    private void handleClear() {
        nameField.clear();
        priceField.clear();
        modifierBox.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    private boolean validate() {
        if (!"admin".equalsIgnoreCase(Session.getRole())) return false;
        try { Double.parseDouble(priceField.getText()); }
        catch (Exception e) { error("Invalid price"); return false; }
        return modifierBox.getValue() != null && !nameField.getText().isBlank();
    }

    private void fill(PreparedStatement ps) throws SQLException {
        ps.setInt(1, modifierBox.getValue().getId());
        ps.setString(2, nameField.getText());
        ps.setDouble(3, Double.parseDouble(priceField.getText()));
    }

    private void disableCrud() {
        modifierBox.setDisable(true);
        nameField.setDisable(true);
        priceField.setDisable(true);
    }

    private void error(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    /* ===== Inline Models ===== */
    public static class Option {
        private final int id, modifierId;
        private final String modifierName, name;
        private final double price;

        public Option(int id, int modifierId, String modifierName, String name, double price) {
            this.id = id;
            this.modifierId = modifierId;
            this.modifierName = modifierName;
            this.name = name;
            this.price = price;
        }
        public int getId() { return id; }
        public int getModifierId() { return modifierId; }
        public String getModifierName() { return modifierName; }
        public String getName() { return name; }
        public double getPrice() { return price; }
    }

    public static class ModifierItem {
        private final int id; private final String name;
        public ModifierItem(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        public String toString() { return name; }
    }
}
