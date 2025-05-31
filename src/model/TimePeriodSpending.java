package model;

public class TimePeriodSpending {
	private String period; // "2025-01" or "2025"
    private double total;

    public TimePeriodSpending(String period, double total) {
        this.period = period;
        this.total = total;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
