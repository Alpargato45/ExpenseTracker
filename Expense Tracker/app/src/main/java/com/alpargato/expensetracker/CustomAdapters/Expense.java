package com.alpargato.expensetracker.CustomAdapters;

public class Expense {

    private double expense;

    private int img;

    private String date;

    private String ExpenseType;

    public Expense(double expense, int img, String date, String expenseType) {
        this.expense = expense;
        this.img = img;
        this.date = date;
        ExpenseType = expenseType;
    }

    public double getExpense() {
        return expense;
    }

    public void setExpense(double expense) {
        this.expense = expense;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getExpenseType() {
        return ExpenseType;
    }

    public void setExpenseType(String expenseType) {
        ExpenseType = expenseType;
    }
}
