package models;

public class Sale {

    private int id;
    private int productId;
    private String productName;
    private int quantity;
    private double total;

    public Sale(int id, int productId, String productName, int quantity, double total) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.total = total;
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

    public int getQuantity() {
        return quantity;
    }

    public double getTotal() {
        return total;
    }
}
