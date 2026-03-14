package edu.univ.erp.domain;

public class GradeRecord {
    private final String studentId;
    private final String rollNo;
    private final String sectionId;

    private Double quiz;
    private Double midterm;
    private Double finalExam;
    private Double finalScore;

    public GradeRecord(String studentId, String rollNo, String sectionId,
                       Double quiz, Double midterm, Double finalExam, Double finalScore) {
        this.studentId = studentId;
        this.rollNo = rollNo;
        this.sectionId = sectionId;
        this.quiz = quiz;
        this.midterm = midterm;
        this.finalExam = finalExam;
        this.finalScore = finalScore;
    }

    public String getStudentId() { return studentId; }
    public String getRollNo() { return rollNo; }
    public String getSectionId() { return sectionId; }

    public Double getQuiz() { return quiz; }
    public void setQuiz(Double quiz) { this.quiz = quiz; }

    public Double getMidterm() { return midterm; }
    public void setMidterm(Double midterm) { this.midterm = midterm; }

    public Double getFinalExam() { return finalExam; }
    public void setFinalExam(Double finalExam) { this.finalExam = finalExam; }

    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }
}
