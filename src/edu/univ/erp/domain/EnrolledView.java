package edu.univ.erp.domain;

public class EnrolledView {
    private final String courseCode;
    private final String courseTitle;
    private final String sectionId;
    private final String dayTime;
    private final String room;
    private final int credits;

    public EnrolledView(String courseCode, String courseTitle, String sectionId, String dayTime, String room, int credits) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.sectionId = sectionId;
        this.dayTime = dayTime;
        this.room = room;
        this.credits = credits;
    }

    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public String getSectionId() { return sectionId; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getCredits() { return credits; }
}