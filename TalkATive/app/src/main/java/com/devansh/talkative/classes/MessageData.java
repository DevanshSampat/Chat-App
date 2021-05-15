package com.devansh.talkative.classes;

public class MessageData {
    private boolean dateChanged;
    private String message;
    private boolean seen = false;
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

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
