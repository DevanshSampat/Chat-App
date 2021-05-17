package com.devansh.talkative.classes;

public class CurrentUserName {
    private static String name;

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        CurrentUserName.name = name;
    }
}
