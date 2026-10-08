package com.alpargato.expensetracker.CustomAdapters;

public class Expense {

    private double expense;
    private String date;
    private String expenseType;
    private String description;
    private String emoji;
    private String firebaseKey;

    public Expense(double expense, String date, String expenseType,
                   String description, String emoji) {
        this(expense, date, expenseType, description, emoji, null);
    }

    public Expense(double expense, String date, String expenseType,
                   String description, String emoji, String firebaseKey) {
        this.expense = expense;
        this.date = date;
        this.expenseType = expenseType;
        this.description = description;
        this.emoji = emoji;
        this.firebaseKey = firebaseKey;
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

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }
}