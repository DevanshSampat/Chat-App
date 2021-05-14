package com.devansh.talkative.classes;

public class UserData {
    private String name,image,uid,email;
    public UserData(String name, String email, String image, String uid){
        this.name = name;
        this.image = image;
        this.uid = uid;
        this.email = email;
    }

    public String toString(){
        return (name+"\t"+email+"\t"+image+"\t"+uid);
    }
    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }
}
