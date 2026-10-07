package com.alpargato.expensetracker.ExpenseType;

public class ExpenseType {

    private String name;
    private String emoji;

    public ExpenseType(String name, String emoji) {
        this.name = name;
        this.emoji = emoji;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
}
