package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.DatabaseConnection;

import java.sql.*;
import java.io.*;
import java.util.Date;

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

    private final ObservableList<SaleReport> salesList
            = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        reportType.getItems().addAll("Daily", "Monthly");
        reportType.setValue("Daily");

        loadSales("daily");
    }

    @FXML
    private void handleGenerateReport() {
        loadSales(reportType.getValue().toLowerCase());
    }

    private void loadSales(String type) {

        salesList.clear();
        double totalSales = 0;
        int transactions = 0;

        String dateFilter
                = type.equals("monthly")
                ? "MONTH(s.created_at)=MONTH(CURDATE()) AND YEAR(s.created_at)=YEAR(CURDATE())"
                : "DATE(s.created_at)=CURDATE()";

        String sql = """
            SELECT
                s.id AS sale_id,
                p.name AS product_name,
                si.quantity,
                si.subtotal,
                s.created_at
            FROM sales s
            JOIN sale_items si ON s.id = si.sale_id
            JOIN products p ON si.product_id = p.id
            WHERE %s
            ORDER BY s.created_at DESC
        """.formatted(dateFilter);

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                salesList.add(new SaleReport(
                        rs.getInt("sale_id"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("subtotal"),
                        rs.getTimestamp("created_at").toString()
                ));

                totalSales += rs.getDouble("subtotal");
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
        try (PrintWriter writer = new PrintWriter("sales_report.csv")) {

            writer.println("Sale ID,Product,Quantity,Subtotal,Date");
            for (SaleReport s : salesList) {
                writer.printf("%d,%s,%d,%.2f,%s%n",
                        s.getSaleId(),
                        s.getProductName(),
                        s.getQuantity(),
                        s.getSubtotal(),
                        s.getDate());
            }

            showAlert(Alert.AlertType.INFORMATION,
                    "CSV exported: sales_report.csv");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    private void handleExportPDF() {

        try {
            Document document = new Document();
            PdfWriter.getInstance(document,
                    new FileOutputStream("sales_report.pdf"));

            document.open();

            document.add(new Paragraph("Sales Report",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph(new Date().toString()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            table.addCell("Sale ID");
            table.addCell("Product");
            table.addCell("Qty");
            table.addCell("Subtotal");
            table.addCell("Date");

            for (SaleReport s : salesList) {
                table.addCell(String.valueOf(s.getSaleId()));
                table.addCell(s.getProductName());
                table.addCell(String.valueOf(s.getQuantity()));
                table.addCell(String.format("₱%.2f", s.getSubtotal()));
                table.addCell(s.getDate());
            }

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(totalSalesLabel.getText()));
            document.add(new Paragraph(transactionCountLabel.getText()));

            document.close();

            showAlert(Alert.AlertType.INFORMATION,
                    "PDF exported: sales_report.pdf");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }

    /* ================== REPORT ROW MODEL ================== */
    public static class SaleReport {

        private final int saleId;
        private final String productName;
        private final int quantity;
        private final double subtotal;
        private final String date;

        public SaleReport(int saleId, String productName,
                int quantity, double subtotal, String date) {
            this.saleId = saleId;
            this.productName = productName;
            this.quantity = quantity;
            this.subtotal = subtotal;
            this.date = date;
        }

        public int getSaleId() {
            return saleId;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getSubtotal() {
            return subtotal;
        }

        public String getDate() {
            return date;
        }
    }
}
