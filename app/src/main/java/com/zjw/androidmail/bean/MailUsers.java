package com.zjw.androidmail.bean;

/**
 * Created by zhaojinwei on 2017/4/5.
 */

public class MailUsers {

    private int id;

    private String name;

    private String address;

    public MailUsers(int id, String name, String address){
        super();
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String address){
        this.address = address;
    }
}
