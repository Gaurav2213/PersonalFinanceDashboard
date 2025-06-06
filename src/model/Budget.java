package model;

	public class Budget {
	    private int id;           // Optional: for updates/deletes
	    private int userId;
	    private String category;
	    private double amount;

	    public Budget() {}  // Default constructor for JSON deserialization

	    public Budget(int id, int userId, String category, double amount) {
	        this.id = id;
	        this.userId = userId;
	        this.category = category;
	        this.amount = amount;
	    }

	    public Budget(int userId, String category, double amount) {
	        this.userId = userId;
	        this.category = category;
	        this.amount = amount;
	    }

	    // Getters and setters

	    public int getId() {
	        return id;
	    }

	    public void setId(int id) {
	        this.id = id;
	    }

	    public int getUserId() {
	        return userId;
	    }

	    public void setUserId(int userId) {
	        this.userId = userId;
	    }

	    public String getCategory() {
	        return category;
	    }

	    public void setCategory(String category) {
	        this.category = category;
	    }

	    public double getAmount() {
	        return amount;
	    }

	    public void setAmount(double amount) {
	        this.amount = amount;
	    }
	}

