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

## ⚙️ Setup Guide

### 1️⃣ Clone the Project

```bash
git clone https://github.com/Jareth-Baur/Simple-POS-System-JavaFX.git
cd Simple-POS-System-JavaFX
```

### 2️⃣ Set Up the Database

1. Open **MySQL** and create a new database:

   ```sql
   CREATE DATABASE pos_system;
   ```
2. Import the provided SQL file:

   ```sql
   source pos_system.sql;
   ```
3. Update your database credentials inside:

   ```
   utils/DatabaseConnection.java
   ```

## Screenshots

<img width="1148" height="727" alt="image" src="https://github.com/user-attachments/assets/fc0d4d02-451a-4c7d-9873-41983e5867c6" />

<img width="1149" height="728" alt="image" src="https://github.com/user-attachments/assets/ce63d964-4fe2-4d24-b1a6-a5f7b7aef558" />
