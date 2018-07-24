package com.id.drapp;


public class charUtility {

    public static String filterString(String input){
        String regx = ".@#$[]";
        char[] ca = regx.toCharArray();
        for (char c : ca) {
            input = input.replace(""+c, "");
        }
        return input;
    }

}
