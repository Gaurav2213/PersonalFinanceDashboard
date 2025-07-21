package model;

public class OverspendWarning {
    private String category;
    private double budget;
    private double spent;
    private double percentUsed;
    private String warningType;  // "Overspent" or "Nearing Limit"

    public OverspendWarning(String category, double budget, double spent, double percentUsed, String warningType) {
        this.category = category;
        this.budget = budget;
        this.spent = spent;
        this.percentUsed = percentUsed;
        this.warningType = warningType;
    }

    // Getters only (optional: add setters if needed)
    public String getCategory() { return category; }
    public double getBudget() { return budget; }
    public double getSpent() { return spent; }
    public double getPercentUsed() { return percentUsed; }
    public String getWarningType() { return warningType; }
}
