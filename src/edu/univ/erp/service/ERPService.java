package edu.univ.erp.service;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.EnrolledView;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.GradeRecord;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ERPService {

    public ERPService() {}

    /* ----------------- Maintenance ----------------- */
    public boolean isMaintenanceOn() {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = 'maintenance'";
        try (Connection conn = DBConnection.getERPConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return "true".equalsIgnoreCase(rs.getString("setting_value"));
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public void setMaintenance(boolean v) {
        String sql = "UPDATE settings SET setting_value = ? WHERE setting_key = 'maintenance'";
        try (Connection conn = DBConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(v));
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /* ----------------- Registration Logic ----------------- */
    public Enrollment registerSection(User user, String studentId, String sectionId) throws Exception {
        if (isMaintenanceOn()) throw new Exception("Maintenance is ON: Registration disabled.");
        if (user.getRole() != User.Role.STUDENT) throw new Exception("Only students can register.");

        Section s = getSectionById(sectionId);
        if (s == null) throw new Exception("Section not found");

        if (s.getRegDeadline() != null && LocalDate.now().isAfter(s.getRegDeadline())) {
            throw new Exception("Registration deadline passed (" + s.getRegDeadline() + ")");
        }

        try (Connection conn = DBConnection.getERPConnection()) {
            String countSql = "SELECT count(*) FROM enrollments WHERE section_id = ? AND status = 'ACTIVE'";
            int current = 0;
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                ps.setString(1, sectionId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) current = rs.getInt(1);
            }
            if (current >= s.getCapacity()) throw new Exception("Section is Full!");

            String dupSql = "SELECT count(*) FROM enrollments WHERE student_id = ? AND section_id = ? AND status = 'ACTIVE'";
            try (PreparedStatement ps = conn.prepareStatement(dupSql)) {
                ps.setString(1, studentId);
                ps.setString(2, sectionId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) throw new Exception("Already registered!");
            }

            String id = "E" + System.currentTimeMillis();
            String ins = "INSERT INTO enrollments (id, student_id, section_id, status) VALUES (?, ?, ?, 'ACTIVE')";
            try (PreparedStatement ps = conn.prepareStatement(ins)) {
                ps.setString(1, id);
                ps.setString(2, studentId);
                ps.setString(3, sectionId);
                ps.executeUpdate();
            }
            return new Enrollment(id, studentId, sectionId);
        }
    }

    /* ----------------- Drop Logic ----------------- */
    public void dropSection(User user, String studentId, String sectionId) throws Exception {
        if (isMaintenanceOn()) throw new Exception("Maintenance is ON: Cannot drop courses.");

        Section s = getSectionById(sectionId);
        if (s != null && s.getDropDeadline() != null) {
            if (LocalDate.now().isAfter(s.getDropDeadline())) {
                throw new Exception("Drop deadline passed (" + s.getDropDeadline() + ")");
            }
        }

        String sql = "UPDATE enrollments SET status = 'DROPPED' WHERE student_id = ? AND section_id = ?";
        try (Connection conn = DBConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, sectionId);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new Exception("Enrollment not found or already dropped.");
        }
    }

    /* ----------------- Hostel & Fees Logic ----------------- */
    public void requestHostel(String studentId, String roomType) throws Exception {
        if (isMaintenanceOn()) throw new Exception("Maintenance ON");
        try (Connection conn = DBConnection.getERPConnection()) {
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS hostel_requests (id INT AUTO_INCREMENT PRIMARY KEY, student_id VARCHAR(50), room_type VARCHAR(20), status VARCHAR(20), request_date DATE)");
            String check = "SELECT count(*) FROM hostel_requests WHERE student_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(check)) {
                ps.setString(1, studentId);
                ResultSet rs = ps.executeQuery();
                if(rs.next() && rs.getInt(1) > 0) throw new Exception("You have already submitted a hostel request.");
            }
            String sql = "INSERT INTO hostel_requests (student_id, room_type, status, request_date) VALUES (?, ?, 'PENDING', CURDATE())";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, studentId); ps.setString(2, roomType); ps.executeUpdate();
            }
        }
    }

    public List<String> getHostelRequests() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT h.student_id, h.room_type, h.status, s.program " +
                "FROM hostel_requests h JOIN students s ON h.student_id = s.user_id";
        try (Connection conn = DBConnection.getERPConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("student_id") + " (" + rs.getString("program") + ") requested " +
                        rs.getString("room_type") + " - Status: " + rs.getString("status"));
            }
        } catch (Exception e) { }
        return list;
    }

    public double calculateTotalFee(String studentId) {
        double costPerCredit = 1500.0;
        double baseFee = 5000.0;
        List<EnrolledView> courses = getStudentSchedule(studentId);
        int totalCredits = courses.stream().mapToInt(EnrolledView::getCredits).sum();
        return baseFee + (totalCredits * costPerCredit);
    }

    /* ----------------- Instructor Logic ----------------- */
    public List<User> listInstructors() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, username FROM users_auth WHERE role = 'INSTRUCTOR'";
        try (Connection conn = DBConnection.getAuthConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new User(rs.getString("user_id"), rs.getString("username"), "", User.Role.INSTRUCTOR));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void assignInstructorToSection(String sectionId, String newInstructorId) throws Exception {
        if (isMaintenanceOn()) throw new Exception("Maintenance is ON.");

        if (newInstructorId == null) {
            try (Connection conn = DBConnection.getERPConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE sections SET instructor_id = NULL WHERE id = ?")) {
                ps.setString(1, sectionId);
                ps.executeUpdate();
            }
            return;
        }

        String currentInstructor = null;
        try (Connection conn = DBConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT instructor_id FROM sections WHERE id = ?")) {
            ps.setString(1, sectionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) currentInstructor = rs.getString("instructor_id");
            else throw new Exception("Section ID not found.");
        }

        if (newInstructorId.equals(currentInstructor)) {
            throw new Exception("This instructor is already assigned to this section.");
        }

        if (currentInstructor != null && !currentInstructor.isEmpty() && !currentInstructor.equals("null")) {
            throw new Exception("Section is already taken by " + currentInstructor + ". Unassign them first.");
        }

        try (Connection conn = DBConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE sections SET instructor_id = ? WHERE id = ?")) {
            ps.setString(1, newInstructorId);
            ps.setString(2, sectionId);
            ps.executeUpdate();
        }
    }

    public List<Section> listSectionsForInstructor(String instructorId) {
        List<Section> list = new ArrayList<>();
        String sql = "SELECT * FROM sections WHERE instructor_id = ?";
        try (Connection conn = DBConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, instructorId);
            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()) list.add(mapSection(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<GradeRecord> getGradebook(String sectionId) {
        List<GradeRecord> list = new ArrayList<>();
        String sql = "SELECT e.student_id, s.roll_no, g.quiz, g.midterm, g.final_exam, g.final_score " +
                "FROM enrollments e " +
                "JOIN students s ON e.student_id = s.user_id " +
                "LEFT JOIN grades g ON e.id = g.enrollment_id " +
                "WHERE e.section_id = ? AND e.status = 'ACTIVE'";

        try (Connection conn = DBConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sectionId);
            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    list.add(new GradeRecord(
                            rs.getString("student_id"),
                            rs.getString("roll_no"),
                            sectionId,
                            (Double) rs.getObject("quiz"),
                            (Double) rs.getObject("midterm"),
                            (Double) rs.getObject("final_exam"),
                            (Double) rs.getObject("final_score")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // --- UPDATED SAVE GRADE WITH SECURITY CHECK ---
    public void saveGrade(User user, String studentId, String sectionId, Double q, Double m, Double f) throws Exception {
        if (isMaintenanceOn()) throw new Exception("Maintenance ON");

        // Security Check
        if (user.getRole() == User.Role.INSTRUCTOR) {
            try (Connection conn = DBConnection.getERPConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM sections WHERE id = ? AND instructor_id = ?")) {
                ps.setString(1, sectionId);
                ps.setString(2, user.getUserId());
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new Exception("Not your section");
                }
            }
        }

        String eid = null;
        try(Connection conn = DBConnection.getERPConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM enrollments WHERE student_id=? AND section_id=?")) {
            ps.setString(1, studentId);
            ps.setString(2, sectionId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) eid = rs.getString(1);
        }
        if (eid == null) return; // Student may have dropped

        double finalScore = 0;
        if(q!=null) finalScore += q * 0.20;
        if(m!=null) finalScore += m * 0.30;
        if(f!=null) finalScore += f * 0.50;

        String sql = "INSERT INTO grades (enrollment_id, quiz, midterm, final_exam, final_score) VALUES (?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE quiz=?, midterm=?, final_exam=?, final_score=?";

        try(Connection conn = DBConnection.getERPConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eid);
            setDouble(ps, 2, q); setDouble(ps, 3, m); setDouble(ps, 4, f); setDouble(ps, 5, finalScore);
            setDouble(ps, 6, q); setDouble(ps, 7, m); setDouble(ps, 8, f); setDouble(ps, 9, finalScore);
            ps.executeUpdate();
        }
    }

    public static class SectionStats {
        public int count; public double avg, min, max;
    }
    public SectionStats getSectionStats(String sectionId) {
        SectionStats s = new SectionStats();
        String sql = "SELECT count(*), avg(final_score), min(final_score), max(final_score) " +
                "FROM grades g JOIN enrollments e ON g.enrollment_id = e.id " +
                "WHERE e.section_id = ?";
        try (Connection conn = DBConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sectionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                s.count = rs.getInt(1);
                s.avg = rs.getDouble(2);
                s.min = rs.getDouble(3);
                s.max = rs.getDouble(4);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return s;
    }

    /* ----------------- User/Admin Logic ----------------- */

    public void createSection(User admin, String id, String courseCode, String courseTitle, int credits,
                              int cap, String sem, int year, String dt, String room,
                              LocalDate regD, LocalDate dropD) throws Exception {

        if (admin.getRole() != User.Role.ADMIN) throw new Exception("Admin only");

        try (Connection conn = DBConnection.getERPConnection()) {

            // 1. Check Section ID first
            String secCheck = "SELECT count(*) FROM sections WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(secCheck)) {
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if(rs.next() && rs.getInt(1) > 0) {
                    throw new Exception("Duplicate entry '" + id + "' for key 'sections.PRIMARY'");
                }
            }

            // 2. Check Course
            String courseCheck = "SELECT title FROM courses WHERE code = ?";
            try (PreparedStatement ps = conn.prepareStatement(courseCheck)) {
                ps.setString(1, courseCode);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String existingTitle = rs.getString("title");
                    if (!existingTitle.equalsIgnoreCase(courseTitle)) {
                        throw new Exception("Conflict: Course Code '" + courseCode + "' already exists as '" + existingTitle + "'.");
                    }
                } else {
                    String insertC = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
                    try (PreparedStatement psi = conn.prepareStatement(insertC)) {
                        psi.setString(1, courseCode);
                        psi.setString(2, courseTitle);
                        psi.setInt(3, credits);
                        psi.executeUpdate();
                    }
                }
            }

            // 3. Create Section
            String sectionSql = "INSERT INTO sections (id, course_code, capacity, semester, year, day_time, room, reg_deadline, drop_deadline) VALUES (?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sectionSql)) {
                ps.setString(1, id);
                ps.setString(2, courseCode);
                ps.setInt(3, cap);
                ps.setString(4, sem);
                ps.setInt(5, year);
                ps.setString(6, dt);
                ps.setString(7, room);
                ps.setObject(8, regD);
                ps.setObject(9, dropD);
                ps.executeUpdate();
            }
        }
    }

    public void deleteSection(String sectionId) throws Exception {
        if (isMaintenanceOn()) throw new Exception("Maintenance is ON.");

        try (Connection conn = DBConnection.getERPConnection()) {
            String checkSql = "SELECT count(*) FROM enrollments WHERE section_id = ? AND status = 'ACTIVE'";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, sectionId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new Exception("Cannot delete section: " + rs.getInt(1) + " active students are currently enrolled.");
                }
            }

            String delGrades = "DELETE FROM grades WHERE enrollment_id IN (SELECT id FROM enrollments WHERE section_id = ?)";
            try (PreparedStatement ps = conn.prepareStatement(delGrades)) {
                ps.setString(1, sectionId);
                ps.executeUpdate();
            }

            String delEnroll = "DELETE FROM enrollments WHERE section_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delEnroll)) {
                ps.setString(1, sectionId);
                ps.executeUpdate();
            }

            String delSection = "DELETE FROM sections WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delSection)) {
                ps.setString(1, sectionId);
                int rows = ps.executeUpdate();
                if (rows == 0) throw new Exception("Section ID not found.");
            }
        }
    }

    public void changePassword(String username, String newHash) throws Exception {
        String sql = "UPDATE users_auth SET password_hash = ? WHERE username = ?";
        try(Connection conn = DBConnection.getAuthConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }

    public List<Section> listSections() {
        List<Section> list = new ArrayList<>();
        String sql = "SELECT s.*, c.title as course_title, c.credits FROM sections s " +
                "LEFT JOIN courses c ON s.course_code = c.code";
        try (Connection conn = DBConnection.getERPConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Section sec = mapSection(rs);
                sec.setCourseTitle(rs.getString("course_title"));
                sec.setCourseCredits(rs.getInt("credits"));
                list.add(sec);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<EnrolledView> getStudentSchedule(String studentId) {
        List<EnrolledView> list = new ArrayList<>();
        String sql = "SELECT c.code, c.title, c.credits, s.id, s.day_time, s.room " +
                "FROM enrollments e JOIN sections s ON e.section_id = s.id " +
                "JOIN courses c ON s.course_code = c.code " +
                "WHERE e.student_id = ? AND e.status = 'ACTIVE'";
        try (Connection conn = DBConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new EnrolledView(rs.getString(1), rs.getString(2), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(3)));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<GradeRow> getStudentGrades(String studentId) {
        List<GradeRow> list = new ArrayList<>();
        String sql = "SELECT c.code, c.title, s.id, g.quiz, g.midterm, g.final_exam, g.final_score " +
                "FROM enrollments e JOIN sections s ON e.section_id = s.id " +
                "JOIN courses c ON s.course_code = c.code " +
                "LEFT JOIN grades g ON e.id = g.enrollment_id " +
                "WHERE e.student_id = ? AND e.status = 'ACTIVE'";
        try (Connection conn = DBConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                list.add(new GradeRow(rs.getString(1), rs.getString(2), rs.getString(3),
                        (Double)rs.getObject(4), (Double)rs.getObject(5), (Double)rs.getObject(6), (Double)rs.getObject(7)));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Helpers
    private Section getSectionById(String id) {
        try (Connection conn = DBConnection.getERPConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM sections WHERE id=?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return mapSection(rs);
        } catch(Exception e) { e.printStackTrace(); }
        return null;
    }

    private Section mapSection(ResultSet rs) throws SQLException {
        Section s = new Section(rs.getString("id"), rs.getString("course_code"), rs.getInt("capacity"), rs.getString("semester"), rs.getInt("year"));
        s.setInstructorId(rs.getString("instructor_id"));
        s.setDayTime(rs.getString("day_time"));
        s.setRoom(rs.getString("room"));
        if (rs.getDate("reg_deadline") != null) s.setRegDeadline(rs.getDate("reg_deadline").toLocalDate());
        if (rs.getDate("drop_deadline") != null) s.setDropDeadline(rs.getDate("drop_deadline").toLocalDate());
        return s;
    }

    // --- THIS IS THE HELPER YOU NEEDED ---
    private void setDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DOUBLE);
        } else {
            ps.setDouble(index, value);
        }
    }

    public static class GradeRow {
        private String courseCode, courseTitle, sectionId;
        private Double quiz, midterm, finalExam, finalScore;
        public GradeRow(String c, String t, String s, Double q, Double m, Double f, Double fs) {
            this.courseCode=c; this.courseTitle=t; this.sectionId=s; this.quiz=q; this.midterm=m; this.finalExam=f; this.finalScore=fs;
        }
        public String getCourseCode() { return courseCode; }
        public String getCourseTitle() { return courseTitle; }
        public String getSectionId() { return sectionId; }
        public Double getQuiz() { return quiz; }
        public Double getMidterm() { return midterm; }
        public Double getFinalExam() { return finalExam; }
        public Double getFinalScore() { return finalScore; }
    }
}