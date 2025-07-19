package model;

import java.util.List;

public class BatchDeleteBudgetRequest {
    private int userId;
    private List<String> categories;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}
