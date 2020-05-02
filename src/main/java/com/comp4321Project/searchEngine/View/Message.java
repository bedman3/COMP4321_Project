package com.comp4321Project.searchEngine.View;

public class Message {
    public String status;
    public String message;
    public String error;

    public Message(String status, String message, String error) {
        this.status = status;
        this.message = message;
        this.error = error;
    }

    @Override
    public String toString() {
        return "Message{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
