package model;

import java.util.List;

public class TransactionResponse {

	 private boolean success;
	    private String message;
	    private List<Transaction> transactions;

	    public TransactionResponse(boolean success, String message, List<Transaction> transactions) {
	        this.success = success;
	        this.message = message;
	        this.transactions = transactions;
	    }

	    public boolean isSuccess() {
	        return success;
	    }

	    public String getMessage() {
	        return message;
	    }

	    public List<Transaction> getTransactions() {
	        return transactions;
	    }
}
