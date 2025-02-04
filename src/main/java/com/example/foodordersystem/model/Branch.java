package com.example.foodordersystem.model;

public class Branch {
    private int id;
    private String branchName;
    private String branchCode;

    public Branch() {
    }

    public Branch(int id, String branchName, String branchCode) {
        this.id = id;
        this.branchName = branchName;
        this.branchCode = branchCode;
    }

    public Branch(String branchName, String branchCode) {
        this.branchName = branchName;
        this.branchCode = branchCode;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
