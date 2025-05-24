package model;

import java.util.List;

public class BatchDeleteRequest {

	private int userId;
    private List<Integer> transactionIds;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<Integer> getTransactionIds() {
        return transactionIds;
    }

    public void setTransactionIds(List<Integer> transactionIds) {
        this.transactionIds = transactionIds;
    }
}
