-- ==========================================
-- 1. SETUP AUTHENTICATION DATABASE
-- ==========================================
DROP DATABASE IF EXISTS university_auth;
CREATE DATABASE university_auth;
USE university_auth;

CREATE TABLE users_auth (
    user_id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    failed_attempts INT DEFAULT 0,
    lockout_end DATETIME DEFAULT NULL,
    last_login DATETIME
);

-- Insert Default Users (Passwords are hashed for security)
INSERT INTO users_auth (user_id, username, role, password_hash) VALUES
('u_admin', 'admin1', 'ADMIN', 'cTv9p4knTWp0ghiKJzdp924JVBZnQwJ1dcqYVrZyYyA='),
('u_inst1', 'inst1', 'INSTRUCTOR', '05k/T/A+JjW+v+M4/F+fC/R/H/X/C/V/B/N/M/='),
('u_stu1', 'stu1', 'STUDENT', 'y/X/c/v/b/n/m/,...'),
('u_stu2', 'stu2', 'STUDENT', 'y/X/c/v/b/n/m/,...');

-- ==========================================
-- 2. SETUP ERP DATA DATABASE
-- ==========================================
DROP DATABASE IF EXISTS university_erp;
CREATE DATABASE university_erp;
USE university_erp;

CREATE TABLE students (
    user_id VARCHAR(50) PRIMARY KEY,
    roll_no VARCHAR(20),
    program VARCHAR(50),
    year INT
);

CREATE TABLE instructors (
    user_id VARCHAR(50) PRIMARY KEY,
    department VARCHAR(50)
);

CREATE TABLE courses (
    code VARCHAR(20) PRIMARY KEY,
    title VARCHAR(100),
    credits INT
);

CREATE TABLE sections (
    id VARCHAR(20) PRIMARY KEY,
    course_code VARCHAR(20),
    instructor_id VARCHAR(50),
    day_time VARCHAR(50),
    room VARCHAR(20),
    capacity INT,
    semester VARCHAR(20),
    year INT,
    reg_deadline DATE,
    drop_deadline DATE,
    FOREIGN KEY (course_code) REFERENCES courses(code)
);

CREATE TABLE enrollments (
    id VARCHAR(50) PRIMARY KEY,
    student_id VARCHAR(50),
    section_id VARCHAR(20),
    status VARCHAR(20),
    FOREIGN KEY (student_id) REFERENCES students(user_id),
    FOREIGN KEY (section_id) REFERENCES sections(id)
);

CREATE TABLE grades (
    enrollment_id VARCHAR(50) PRIMARY KEY,
    quiz DOUBLE,
    midterm DOUBLE,
    final_exam DOUBLE,
    final_score DOUBLE,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id)
);

CREATE TABLE settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(50)
);

CREATE TABLE hostel_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50),
    room_type VARCHAR(20),
    status VARCHAR(20),
    request_date DATE
);

-- ==========================================
-- 3. INSERT SAMPLE DATA
-- ==========================================
INSERT INTO settings (setting_key, setting_value) VALUES ('maintenance', 'false');

INSERT INTO students (user_id, roll_no, program, year) VALUES
('u_stu1', '2023001', 'B.Tech CSE', 2),
('u_stu2', '2023002', 'B.Tech ECE', 2);

INSERT INTO instructors (user_id, department) VALUES
('u_inst1', 'Computer Science');

INSERT INTO courses (code, title, credits) VALUES
('CS101', 'Intro to Java', 4),
('CS202', 'Data Structures', 4),
('MTH101', 'Calculus I', 3);

INSERT INTO sections (id, course_code, instructor_id, day_time, room, capacity, semester, year, reg_deadline, drop_deadline) VALUES
('S1', 'CS101', 'u_inst1', 'Mon 10:00', 'C21', 50, 'Fall', 2026, '2026-12-30', '2026-12-30'),
('S2', 'CS202', 'u_inst1', 'Tue 14:00', 'C22', 40, 'Fall', 2026, '2026-12-30', '2026-12-30');