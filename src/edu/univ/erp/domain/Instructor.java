package edu.univ.erp.domain;

public class Instructor{
    private final String userId;
    private final String department;
    public Instructor(String userId,String department){ this.userId=userId; this.department=department; }
    public String getUserId(){ return userId; }
    public String getDepartment(){ return department; }
}
