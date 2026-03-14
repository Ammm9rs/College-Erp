package edu.univ.erp.domain;

public class Student {
    private final String userId;
    private final String rollNo;
    private final String program;
    private int year;

    public Student(String userId, String rollNo, String program, int year) {
        this.userId = userId; this.rollNo = rollNo; this.program = program; this.year = year;
    }
    public String getUserId(){ return userId; }
    public String getRollNo(){ return rollNo; }
    public String getProgram(){ return program; }
    public int getYear(){ return year; }
    public void setYear(int y){ this.year = y; }
}
