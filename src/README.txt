
Project: University ERP (Java + Swing)
Developers: Ammar (2024065) & Krishna (2024312)
1. System Requirements
Java JDK: Version 17 or higher.
Database: MySQL Server (Version 8.0+).
IDE: IntelliJ IDEA (Recommended) or Eclipse.
2. Database Setup (One-Time Setup)
Open your MySQL Terminal or Workbench.
Copy the content of the attached full_database_setup.sql file.
Paste and execute the script.
This will create two databases (university_auth & university_erp) and populate them with sample data.
3. Application Configuration
Open the project in IntelliJ.
Navigate to: src/edu/univ/erp/data/DBConnection.java.
Update the following lines with your local MySQL credentials:
code
Java
private static final String DB_USER = "root"; // Your MySQL Username
private static final String DB_PASS = "your_password"; // Your MySQL Password
4. How to Run
Navigate to src/edu/univ/erp/ui/MainApp.java.
Right-click the file and select Run 'MainApp.main()'.
5. Default Test Accounts
Use these credentials to test the different roles:
Role	Username	Password	Functionality to Test
Admin	admin1	adminpass	Create sections, Add users, Maintenance toggle.
Instructor	inst1	instpass	Gradebook, View Sections, Export CSV.
Student	stu1	stupass	Register, Drop, View Grades, Timetable.