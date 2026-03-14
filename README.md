# 🎓 University ERP System

A comprehensive, desktop-based Enterprise Resource Planning (ERP) application designed to manage core academic processes for a university. Built with **Java (Swing)** and **MySQL**, this system provides a secure, role-based platform for Students, Instructors, and Administrators.

Developed by **Ammar (2024065)** & **Krishna (2024312)**.

## ✨ Key Features

### 🔒 Security & Architecture
* **Dual-Database System:** Strict separation of concerns. `university_auth` handles login credentials, while `university_erp` handles academic data.
* **Cryptography:** Passwords are never stored in plain text; they are secured using **SHA-256 Hashing**.
* **Brute-Force Protection:** Accounts automatically lock for 5 minutes after 5 failed login attempts.
* **Maintenance Mode:** Admins can instantly lock the system to "Read-Only" mode for students and instructors.

### 👨‍🎓 Student Portal
* **Course Registration:** Live catalog with capacity checks, duplicate-entry prevention, and deadline enforcement.
* **Interactive Timetable:** Auto-generated weekly class schedule grid.
* **Academic Tracking:** View real-time grades and download transcripts.
* **Campus Services:** Generate fee invoices and submit hostel room requests.

### 👨‍🏫 Instructor Portal
* **Smart Gradebook:** View assigned sections and manage enrolled students.
* **Automated Grading:** Enter Quiz, Midterm, and Exam scores to automatically compute weighted Final Grades (20/30/50 split).
* **Data Portability:** Export gradebooks to `.csv` for Excel, and import them back into the system.
* **Class Analytics:** Instantly view class averages, highest, and lowest scores.

### 🛠️ Admin Portal
* **System Management:** Create courses and sections with specific capacities, timings, and deadlines.
* **User Management:** Create and onboard new students and instructors.
* **Resource Allocation:** Assign instructors to specific sections and review student hostel requests.
* **Database Tools:** Built-in SQL Backup and Restore functionality directly from the GUI.

## 💻 Tech Stack
* **Frontend:** Java Swing (UI enhanced with [FlatLaf](https://www.formdev.com/flatlaf/) for a modern look)
* **Backend:** Java (JDK 17+)
* **Database:** MySQL 8.0+
* **Connectivity:** JDBC (Java Database Connectivity)

## 🚀 Installation & Setup

**1. Clone the repository**
```bash
git clone https://github.com/Ammm9rs/College-Erp.git
cd College-Erp
