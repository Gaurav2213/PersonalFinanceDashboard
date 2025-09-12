package model;

import java.util.List;

public class AnalyticsResponse<T> {

	 private boolean success;
	    private String message;
	    private T data;

	    // Constructor
	    public AnalyticsResponse(boolean success, String message, T data) {
	        this.success = success;
	        this.message = message;
	        this.data = data;
	    }
	    //constructor without data initilization
	    public AnalyticsResponse(boolean success, String message) {
	        this(success, message, null);
	    }


		// Getters & Setters
	    public boolean isSuccess() {
	        return success;
	    }

	    public void setSuccess(boolean success) {
	        this.success = success;
	    }

	    public String getMessage() {
	        return message;
	    }

	    public void setMessage(String message) {
	        this.message = message;
	    }

	    public T getData() {
	        return data;
	    }

	    public void setData(T data) {
	        this.data = data;
	    }
}
