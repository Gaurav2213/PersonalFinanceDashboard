package model;

import java.util.Map;

public class CategoryTrend {
    private String month; // e.g., "2024-04"
    private Map<String, Double> categorySpending;

    public CategoryTrend(String month, Map<String, Double> categorySpending) {
        this.month = month;
        this.categorySpending = categorySpending;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Map<String, Double> getCategorySpending() {
        return categorySpending;
    }

    public void setCategorySpending(Map<String, Double> categorySpending) {
        this.categorySpending = categorySpending;
    }
}
