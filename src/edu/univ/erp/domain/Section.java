package edu.univ.erp.domain;

import java.time.LocalDate;

public class Section {

    private final String id;
    private final String courseCode;
    private String courseTitle;
    private int courseCredits; // <--- NEW FIELD
    private String instructorId;
    private String dayTime;
    private String room;
    private int capacity;
    private String semester;
    private int year;

    private LocalDate regDeadline;
    private LocalDate dropDeadline;

    public Section(String id, String courseCode, int capacity, String semester, int year) {
        this.id = id;
        this.courseCode = courseCode;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public String getCourseCode() { return courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String t) { this.courseTitle = t; }

    // NEW: Credits
    public int getCourseCredits() { return courseCredits; }
    public void setCourseCredits(int c) { this.courseCredits = c; }

    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int c) { this.capacity = c; }

    public String getSemester() { return semester; }
    public int getYear() { return year; }

    public String getDayTime() { return dayTime; }
    public void setDayTime(String dt) { this.dayTime = dt; }

    public String getRoom() { return room; }
    public void setRoom(String r) { this.room = r; }

    public LocalDate getRegDeadline() { return regDeadline; }
    public void setRegDeadline(LocalDate regDeadline) { this.regDeadline = regDeadline; }

    public LocalDate getDropDeadline() { return dropDeadline; }
    public void setDropDeadline(LocalDate dropDeadline) { this.dropDeadline = dropDeadline; }
}