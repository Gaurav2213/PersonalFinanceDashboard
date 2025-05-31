package model;

public class BudgetUtilization {

	  private String category;
	    private double budget;
	    private double spent;
	    private double percentUsed;

	    public BudgetUtilization(String category, double budget, double spent, double percentUsed) {
	        this.category = category;
	        this.budget = budget;
	        this.spent = spent;
	        this.percentUsed = percentUsed;
	    }

	    public String getCategory() {
	        return category;
	    }

	    public void setCategory(String category) {
	        this.category = category;
	    }

	    public double getBudget() {
	        return budget;
	    }

	    public void setBudget(double budget) {
	        this.budget = budget;
	    }

	    public double getSpent() {
	        return spent;
	    }

	    public void setSpent(double spent) {
	        this.spent = spent;
	    }

	    public double getPercentUsed() {
	        return percentUsed;
	    }

	    public void setPercentUsed(double percentUsed) {
	        this.percentUsed = percentUsed;
	    }
}
