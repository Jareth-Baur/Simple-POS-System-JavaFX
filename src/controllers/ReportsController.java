package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class ReportsController {

    @FXML
    private TableView<SaleReport> salesTable;
    @FXML
    private TableColumn<SaleReport, Integer> colId;
    @FXML
    private TableColumn<SaleReport, String> colProduct;
    @FXML
    private TableColumn<SaleReport, Integer> colQuantity;
    @FXML
    private TableColumn<SaleReport, Double> colTotal;
    @FXML
    private TableColumn<SaleReport, String> colDate;
    @FXML
    private ChoiceBox<String> reportType;
    @FXML
    private Label totalSalesLabel;
    @FXML
    private Label transactionCountLabel;

    private ObservableList<SaleReport> salesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        reportType.getItems().addAll("Daily", "Monthly");
        reportType.setValue("Daily");

        loadSales("daily");
    }

    @FXML
    private void handleGenerateReport() {
        String selected = reportType.getValue().toLowerCase();
        loadSales(selected);
    }

    private void loadSales(String type) {
        salesList.clear();
        double totalSales = 0;
        int transactions = 0;

        String sql = switch (type) {
            case "monthly" ->
                "SELECT s.id, p.name AS product_name, s.quantity, s.total, s.created_at "
                + "FROM sales s JOIN products p ON s.product_id=p.id "
                + "WHERE MONTH(s.created_at)=MONTH(CURDATE()) AND YEAR(s.created_at)=YEAR(CURDATE())";
            default ->
                "SELECT s.id, p.name AS product_name, s.quantity, s.total, s.created_at "
                + "FROM sales s JOIN products p ON s.product_id=p.id "
                + "WHERE DATE(s.created_at)=CURDATE()";
        };

        try (Connection conn = DatabaseConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                salesList.add(new SaleReport(
                        rs.getInt("id"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("total"),
                        rs.getTimestamp("created_at").toString()
                ));
                totalSales += rs.getDouble("total");
                transactions++;
            }

            salesTable.setItems(salesList);
            totalSalesLabel.setText(String.format("Total Sales: ₱%.2f", totalSales));
            transactionCountLabel.setText("Transactions: " + transactions);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportCSV() {
        try {
            File file = new File("sales_report.csv");
            PrintWriter writer = new PrintWriter(file);

            writer.println("Sale ID,Product,Quantity,Total,Date");
            for (SaleReport s : salesList) {
                writer.printf("%d,%s,%d,%.2f,%s%n",
                        s.getId(), s.getProductName(), s.getQuantity(), s.getTotal(), s.getDate());
            }

            writer.close();
            showAlert(Alert.AlertType.INFORMATION, "Report exported as sales_report.csv");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    private void handleExportPDF() {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("sales_report.pdf"));
            document.open();

            document.add(new Paragraph("Sales Report",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph(new Date().toString()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.addCell("Sale ID");
            table.addCell("Product");
            table.addCell("Quantity");
            table.addCell("Total");
            table.addCell("Date");

            for (SaleReport s : salesList) {
                table.addCell(String.valueOf(s.getId()));
                table.addCell(s.getProductName());
                table.addCell(String.valueOf(s.getQuantity()));
                table.addCell(String.format("₱%.2f", s.getTotal()));
                table.addCell(s.getDate());
            }

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(totalSalesLabel.getText()));
            document.add(new Paragraph(transactionCountLabel.getText()));

            document.close();
            showAlert(Alert.AlertType.INFORMATION, "Report exported as sales_report.pdf");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type, msg, ButtonType.OK);
        alert.showAndWait();
    }

    // Inner class for displaying sales
    public static class SaleReport {

        private int id;
        private String productName;
        private int quantity;
        private double total;
        private String date;

        public SaleReport(int id, String productName, int quantity, double total, String date) {
            this.id = id;
            this.productName = productName;
            this.quantity = quantity;
            this.total = total;
            this.date = date;
        }

        public int getId() {
            return id;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getTotal() {
            return total;
        }

        public String getDate() {
            return date;
        }
    }
}
