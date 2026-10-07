package com.alpargato.expensetracker.CustomAdapters;

public class Expense {

    private double expense;
    private String date;

    private String expenseType;

    private String description;

    private String emoji;


    public Expense(double expense, String date, String expenseType, String description, String emoji) {
        this.expense = expense;
        this.date = date;
        this.expenseType = expenseType;
        this.description = description;
        this.emoji = emoji;
    }

    public double getExpense() {
        return expense;
    }

    public void setExpense(double expense) {
        this.expense = expense;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
}
