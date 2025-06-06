package model;

public class BudgetResponse<T> {
    private boolean success;
    private String message;
    private T data;

    // Constructor
    public BudgetResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    //used to send the budget response 
    public BudgetResponse(boolean success, String message) {
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
