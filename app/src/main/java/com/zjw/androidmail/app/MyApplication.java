package com.zjw.androidmail.app;

import android.app.Application;

import com.zjw.androidmail.bean.MailInfo;

import java.io.InputStream;
import java.util.ArrayList;

import javax.mail.Session;
import javax.mail.Store;

/**
 * Created by zhaojinwei on 2017/4/4.
 */

public class MyApplication extends Application {

    public static Session session = null;

    private static Store store;

    public static Store getStore(){
        return store;
    }

    public static void setStore(Store store){
        MyApplication.store = store;
    }

    public static MailInfo info = new MailInfo();

    private ArrayList<InputStream> attachmentInputStream;

    public ArrayList<InputStream> getAttachmentInputStream(){
        return attachmentInputStream;
    }

    public void setAttachmentInputStream(ArrayList<InputStream> attachmentInputStream){
        this.attachmentInputStream = attachmentInputStream;
    }
}
