package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.DatabaseConnection;
import utils.Session;

import java.sql.*;

public class CategoriesController {

    @FXML private TableView<Category> table;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TextField nameField;

    private final ObservableList<Category> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        table.setItems(list);
        load();

        table.setOnMouseClicked(e -> {
            Category c = table.getSelectionModel().getSelectedItem();
            if (c != null) nameField.setText(c.getName());
        });

        if (!"admin".equalsIgnoreCase(Session.getRole())) {
            nameField.setDisable(true);
        }
    }

    private void load() {
        list.clear();
        try (Connection c = DatabaseConnection.getConnection();
             ResultSet rs = c.createStatement()
                     .executeQuery("SELECT * FROM categories")) {

            while (rs.next()) {
                list.add(new Category(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        } catch (Exception e) {
            alert(e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        if (!isAdmin()) return;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     c.prepareStatement("INSERT INTO categories(name) VALUES (?)")) {

            ps.setString(1, nameField.getText());
            ps.executeUpdate();
            load();
            handleClear();

        } catch (Exception e) {
            alert(e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (!isAdmin()) return;

        Category c = table.getSelectionModel().getSelectedItem();
        if (c == null) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement("UPDATE categories SET name=? WHERE id=?")) {

            ps.setString(1, nameField.getText());
            ps.setInt(2, c.getId());
            ps.executeUpdate();
            load();

        } catch (Exception e) {
            alert(e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (!isAdmin()) return;

        Category c = table.getSelectionModel().getSelectedItem();
        if (c == null) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement("DELETE FROM categories WHERE id=?")) {

            ps.setInt(1, c.getId());
            ps.executeUpdate();
            load();

        } catch (Exception e) {
            alert("Category is in use by products.");
        }
    }

    @FXML
    private void handleClear() {
        nameField.clear();
        table.getSelectionModel().clearSelection();
    }

    private boolean isAdmin() {
        return "admin".equalsIgnoreCase(Session.getRole());
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    /* ===== Inline Model ===== */
    public static class Category {
        private final int id;
        private final String name;

        public Category(int id, String name) {
            this.id = id;
            this.name = name;
        }
        public int getId() { return id; }
        public String getName() { return name; }
    }
}
