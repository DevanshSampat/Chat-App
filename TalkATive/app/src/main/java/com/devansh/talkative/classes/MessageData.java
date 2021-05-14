package com.devansh.talkative.classes;

public class MessageData {
    private boolean dateChanged;
    private String message;
    public MessageData(String message, boolean dateChanged){
        this.message = message;
        this.dateChanged = dateChanged;
    }

    public boolean isDateChanged() {
        return dateChanged;
    }

    public String getMessage() {
        return message;
    }
}
