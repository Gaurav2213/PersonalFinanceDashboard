package model;

public class SpendingDistribution {
    private double income;
    private double expense;
    private double incomePercentage;
    private double expensePercentage;

    public SpendingDistribution(double income, double expense, double incomePercentage, double expensePercentage) {
        this.income = income;
        this.expense = expense;
        this.incomePercentage = incomePercentage;
        this.expensePercentage = expensePercentage;
    }

    // Getters
    public double getIncome() { return income; }
    public double getExpense() { return expense; }
    public double getIncomePercentage() { return incomePercentage; }
    public double getExpensePercentage() { return expensePercentage; }

    // Setters (if needed, optional)
}
