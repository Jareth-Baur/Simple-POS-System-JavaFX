package models;

public class Product {

    private int id;
    private String name;
    private String category;
    private String sku;
    private String barcode;
    private double cost;
    private double price;
    private int stock;

    /* ===== FULL CONSTRUCTOR (ADMIN / PRODUCTS / REPORTS) ===== */
    public Product(int id, String name, String category,
            String sku, String barcode,
            double cost, double price, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.sku = sku;
        this.barcode = barcode;
        this.cost = cost;
        this.price = price;
        this.stock = stock;
    }

    /* ===== LIGHTWEIGHT CONSTRUCTOR (POS / SALES) ===== */
    public Product(int id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.category = null;
        this.sku = null;
        this.barcode = null;
        this.cost = 0;
        this.price = price;
        this.stock = stock;
    }

    /* ===== GETTERS (TableView requires these) ===== */
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getSku() {
        return sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public double getCost() {
        return cost;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }
}
