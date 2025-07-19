package model;

public class DateCumulativeSpending {
    private String date;               // e.g., "2025-07-15" or "2025-W29"
    private double cumulativeTotal;

    public DateCumulativeSpending(String date, double cumulativeTotal) {
        this.date = date;
        this.cumulativeTotal = cumulativeTotal;
    }

    // Getters and setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getCumulativeTotal() {
        return cumulativeTotal;
    }

    public void setCumulativeTotal(double cumulativeTotal) {
        this.cumulativeTotal = cumulativeTotal;
    }
}
