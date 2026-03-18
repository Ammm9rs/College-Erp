# 🎓 University ERP System

A comprehensive, desktop-based Enterprise Resource Planning (ERP) application designed to manage core academic processes for a university. Built with **Java (Swing)** and **MySQL**, this system provides a secure, role-based platform for Students, Instructors, and Administrators.

Developed by **Ammar (2024065)** & **Krishna (2024312)**.

---

## 🎯 Objective

To design and implement a modular ERP system that simulates real-world university workflows with secure authentication and role-based access control.

---

## ✨ Key Features

### 🔒 Security & Architecture
- **Dual-Database System:** `university_auth` handles authentication, while `university_erp` handles academic data.
- **Cryptography:** Passwords are stored using **SHA-256 hashing** (with legacy support for plain-text passwords).
- **Brute-Force Protection:** Accounts lock for 5 minutes after 5 failed login attempts.
- **Maintenance Mode:** Admins can switch the system to read-only mode.

### 👨‍🎓 Student Portal
- Course registration with validation and deadlines
- Auto-generated timetable
- View grades and transcripts
- Fee invoices and hostel requests

### 👨‍🏫 Instructor Portal
- Manage sections and students
- Automated grade calculation (20/30/50 split)
- CSV export/import
- Class performance analytics

### 🛠️ Admin Portal
- Manage courses, sections, and schedules
- Create users (students/instructors)
- Assign instructors and manage hostel requests
- GUI-based database backup/restore

---

## 💻 Tech Stack

- **Frontend:** Java Swing (FlatLaf UI)
- **Backend:** Java (JDK 17+)
- **Database:** MySQL 8.0+
- **Connectivity:** JDBC

---

## 📦 Requirements

- Java JDK 17+
- MySQL Server 8.0+
- IntelliJ IDEA (recommended)

---

## 🚀 How to Run the Project

### Step 0: Configure Database Connection

Open `DBConnection.java` and update:
jdbc:mysql://localhost:3306/university_auth
username: root
password: your_password_here

---

### Step 1: Setup Database

1. Open MySQL terminal  
2. Run:
SOURCE full_database_setup.sql;

---

### Step 2: Sample Login Credentials

| Role       | Username | Password  |
|------------|---------|----------|
| Admin      | admin1  | admin123 |
| Instructor | inst1   | inst123  |
| Student    | stu1    | stu123   |

---

### Step 3: Run the Application

Run:
Main.java

---

## 🗄️ Database Initialization

The SQL script will:
- Create `university_auth` and `university_erp` databases
- Create required tables
- Insert sample users and data

---

## 📸 Screenshots

login screen
<img width="1204" height="755" alt="Screenshot 2026-03-19 at 1 55 13 AM" src="https://github.com/user-attachments/assets/fa758c4c-bea4-46a6-817f-e0ee3312433f" />

home screen(admin)
<img width="1279" height="800" alt="Screenshot 2026-03-19 at 1 55 44 AM" src="https://github.com/user-attachments/assets/d5b99028-3c34-43dd-a536-8c12ae83f5e2" />

home screen(student)
<img width="1279" height="800" alt="Screenshot 2026-03-19 at 1 59 15 AM" src="https://github.com/user-attachments/assets/b7ca252e-91db-4b92-8cb7-9f1fa3b68e7c" />

time table(student)
<img width="1279" height="800" alt="Screenshot 2026-03-19 at 1 57 22 AM" src="https://github.com/user-attachments/assets/6678812b-a43e-4321-a84f-0dc504384d57" />

grades(student)
<img width="1279" height="800" alt="Screenshot 2026-03-19 at 1 57 44 AM" src="https://github.com/user-attachments/assets/ab9fe0ae-4e29-4182-864b-090e11480167" />

courses(student)
<img width="1279" height="800" alt="Screenshot 2026-03-19 at 1 58 00 AM" src="https://github.com/user-attachments/assets/a08f1dab-1acc-4180-a5cb-7e93ce322020" />

section & grading(instructor)
<img width="1279" height="800" alt="Screenshot 2026-03-19 at 2 01 30 AM" src="https://github.com/user-attachments/assets/5afa34cb-1b70-4514-a37c-ddfb1dd79f7d" />


---

## 📌 Notes

- Ensure MySQL is running before starting the app
- Update DB credentials before first run
- Default users are preloaded for testing
