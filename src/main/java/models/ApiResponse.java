package models;

import org.jetbrains.annotations.Nullable;

public class ApiResponse {
    private String message;
    @Nullable
    private Object[] data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object[] getData() {
        return data;
    }

    public void setData(Object[] data) {
        this.data = data;
    }
}
