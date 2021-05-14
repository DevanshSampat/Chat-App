package com.devansh.talkative.classes;

public class ChatData {
    String name,uid,image,message;
    public ChatData(String name, String image, String uid, String message){
        this.name = name;
        this.uid = uid;
        this.image = image;
        this.message = message;
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
