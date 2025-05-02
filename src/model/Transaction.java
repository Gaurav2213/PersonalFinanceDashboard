package model;

import java.sql.Date;

public class Transaction {
	 private int id;
	    private int userId;
	    private String type;         // 'income' or 'expense'
	    private double amount;
	    private String category;
	    private String description;
	    private Date date;

	    // Constructor without ID (used when inserting new transactions)
	    public Transaction(int userId, String type, double amount, String category, String description, Date date) {
	        this.userId = userId;
	        this.type = type;
	        this.amount = amount;
	        this.category = category;
	        this.description = description;
	        this.date = date;
	    }

	    // Constructor with ID (used when fetching from DB)
	    public Transaction(int id, int userId, String type, double amount, String category, String description, Date date) {
	        this.id = id;
	        this.userId = userId;
	        this.type = type;
	        this.amount = amount;
	        this.category = category;
	        this.description = description;
	        this.date = date;
	    }

	    // Getters and Setters
	    public int getId() { return id; }
	    public void setId(int id) { this.id = id; }

	    public int getUserId() { return userId; }
	    public void setUserId(int userId) { this.userId = userId; }

	    public String getType() { return type; }
	    public void setType(String type) { this.type = type; }

	    public double getAmount() { return amount; }
	    public void setAmount(double amount) { this.amount = amount; }

	    public String getCategory() { return category; }
	    public void setCategory(String category) { this.category = category; }

	    public String getDescription() { return description; }
	    public void setDescription(String description) { this.description = description; }

	    public Date getDate() { return date; }
	    public void setDate(Date date) { this.date = date; }
}
