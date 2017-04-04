package com.zjw.androidmail.bean;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Created by zhaojinwei on 2017/4/4.
 */

public class MyAuthenticator extends Authenticator {

    String username = null;
    String password = null;

    public MyAuthenticator(String username, String password){
        this.username = username;
        this.password = password;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication(){
        return new PasswordAuthentication(username,password);
    }

}
