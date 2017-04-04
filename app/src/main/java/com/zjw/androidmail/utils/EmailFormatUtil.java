package com.zjw.androidmail.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaojinwei on 2017/4/4.
 */

public class EmailFormatUtil {

    public static Boolean emailFormat(String email){
        boolean tag = true;
        String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(pattern1);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.find()){
            tag = false;
        }
        return tag;
    }
}
