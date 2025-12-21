# 📦 Simple POS System (JavaFX)

A desktop-based **Point of Sale (POS) System** developed using **JavaFX** and **MySQL (MariaDB)**.  

---

## 🚀 Features

### 🔐 Authentication
- Login and Registration
- Role-based access:
  - **Admin** – Full CRUD access + Reports
  - **Cashier** – POS (Sales) only

---

### 🛍 Product Management (Admin)
- Categories CRUD
- Suppliers CRUD
- Products CRUD
- Product Variants
- Product Modifiers & Options
- Product Bundles
- Soft delete for products (`is_active`)

All product-related CRUD operations are grouped under a **Product Management module**.

---

### 💵 Point of Sale (POS)
- Browse available products
- Add products to cart
- Merge quantities for the same product
- Prevent overselling using stock validation
- Real-time total calculation
- Transaction-safe checkout
- Automatic stock deduction after checkout

---

### 📊 Sales Reports (Admin)
- Daily sales report
- Monthly sales report
- Summary information:
  - Total sales amount
  - Number of transactions
- Export reports to:
  - CSV
  - PDF

---

## 🧱 Technologies Used

- **Zulu JDKFX 21**
- **FXML**
- **MySQL / MariaDB**
- **JDBC**
- **iText (PDF export)**

---

## 🗄 Database Schema

Main tables used in the system:

- `users`
- `categories`
- `suppliers`
- `products`
- `product_variants`
- `product_modifiers`
- `product_modifier_options`
- `product_bundles`
- `sales`
- `sale_items`

Foreign key constraints and database transactions are used to maintain data integrity.

---
