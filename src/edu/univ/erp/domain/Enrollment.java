package edu.univ.erp.domain;

public class Enrollment {
    public enum Status { ACTIVE, DROPPED, COMPLETED }
    private final String id;
    private final String studentId;
    private final String sectionId;
    private Status status;

    public Enrollment(String id, String studentId, String sectionId) {
        this.id = id; this.studentId = studentId; this.sectionId = sectionId; this.status = Status.ACTIVE;
    }
    public String getId(){ return id; }
    public String getStudentId(){ return studentId; }
    public String getSectionId(){ return sectionId; }
    public Status getStatus(){ return status; }
    public void setStatus(Status s){ this.status = s; }
}

