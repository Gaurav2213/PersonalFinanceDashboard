package model;

import java.util.List;

public class RecurringTransaction {
    private String category;
    private double amount;
    private List<String> recurringDates;

    public RecurringTransaction(String category, double amount, List<String> recurringDates) {
        this.category = category;
        this.amount = amount;
        this.recurringDates = recurringDates;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public List<String> getRecurringDates() {
        return recurringDates;
    }
}
