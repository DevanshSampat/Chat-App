package com.devansh.talkative.classes;

public class ChatData {
    private String name,uid,image,message;
    private boolean unread = false;
    private boolean seen = false;
    public ChatData(String name, String image, String uid, String message){
        this.name = name;
        this.uid = uid;
        this.image = image;
        this.message = message;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public String getImage() {
        return image;
    }

    public String getMessage() {
        return message;
    }
}
