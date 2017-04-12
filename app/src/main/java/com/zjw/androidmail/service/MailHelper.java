package com.zjw.androidmail.service;

import android.content.Context;
import android.util.Log;

import com.zjw.androidmail.app.MyApplication;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

/**
 * Created by zhaojinwei on 2017/4/7.
 */

public class MailHelper {

    private static MailHelper instance;

    private List<MailReceiver> mailList;

    private HashMap<String, Integer> serviceHashMap;

    private Context context;

    public static MailHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MailHelper(context);
        }
        return instance;
    }

    private MailHelper(Context context) {
        this.context = context;
    }

    public String getUpdateUrlStr() throws Exception {
        String urlStr = null;
        if (serviceHashMap == null) {
            serviceHashMap = this.getServeHashMap();
        }
        if (serviceHashMap.get("update") == 1) {
            urlStr = mailList.get(1).getSubject();
        }
        return urlStr;
    }

    public String getUserHelp() throws Exception {
        String userandmoney = null;
        if (serviceHashMap == null) {
            serviceHashMap = this.getServeHashMap();
        }
        if (serviceHashMap.get("userhelp") == 1) {
            userandmoney = mailList.get(3).getSubject();
        }
        return userandmoney;
    }

    public int getAllUserHelp() throws Exception {
        String userandmoney = null;
        int money = 0;
        if (serviceHashMap == null) {
            serviceHashMap = this.getServeHashMap();
        }
        if (serviceHashMap.get("userhelp") == 1) {
            userandmoney = mailList.get(3).getSubject();
        }
        if (userandmoney != null && userandmoney.contains("all-user-100")) {
            money = Integer.parseInt(userandmoney.substring(userandmoney.lastIndexOf("-" + 1),
                    userandmoney.length()));
        }
        return money;
    }

    public boolean getAdControl() throws Exception {
        String ad = null;
        if (serviceHashMap == null) {
            serviceHashMap = this.getServeHashMap();
        }
        if (serviceHashMap.get("adcontrol") == 1) {
            ad = mailList.get(2).getSubject();
        }
        if (ad.equals("ad=close")) {
            return false;
        }
        return true;
    }

    public HashMap<String, Integer> getServeHashMap() throws Exception {
        serviceHashMap = new HashMap<String, Integer>();
        if (mailList == null) {
            mailList = getAllMail("INBOX");
        }
        String serviceStr = mailList.get(0).getSubject();
        if (serviceStr.contains("update 1.0=true")) {
            serviceHashMap.put("update", 1);
        } else if (serviceStr.contains("update 1.0=false")) {
            serviceHashMap.put("update", 0);
        }
        if (serviceStr.contains("adcontrol 1.0=true")) {
            serviceHashMap.put("adcontrol", 1);
        } else if (serviceStr.contains("adcontrol 1.0=false")) {
            serviceHashMap.put("adcontrol", 0);
        }
        if (serviceStr.contains("userhelp 1.0=true")) {
            serviceHashMap.put("userhelp", 1);
        } else if (serviceStr.contains("userhelp 1.0=false")) {
            serviceHashMap.put("userhelp", 0);
        }
        return serviceHashMap;
    }

    public List<MailReceiver> getAllMail(String folderName) throws MessagingException {
        List<MailReceiver> mailList = new ArrayList<MailReceiver>();

        // 连接服务器
        //Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        Properties props = System.getProperties();
        props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.pop3.socketFactory.fallback", "false");
        props.setProperty("mail.pop3.port", "995");
        props.setProperty("mail.pop3.socketFactory.port", "995");

        final Session session = Session.getInstance(props, null);
        final URLName urlName = new URLName("pop3", "pop.qq.com", 995, null, MyApplication.info.getUsername(), MyApplication.info.getPassword());
        Store store=session.getStore(urlName);
        store.connect();

        Folder inbox = store.getFolder(folderName);
        inbox.open(Folder.READ_ONLY);
        FetchProfile profile = new FetchProfile();
        profile.add(FetchProfile.Item.ENVELOPE);

        int mailCount = inbox.getMessageCount();
        Log.d("count=", mailCount+ "");
        if (mailCount == 0) {
            inbox.close(true);
            store.close();
            return null;
        } else {
            // 取得所有的邮件
            Message[] messages = inbox.getMessages();
            inbox.fetch(messages, profile);
            for (int i = 0; i < messages.length; i++) {
                // 自定义的邮件对象
                MailReceiver receiveMail = new MailReceiver((MimeMessage) messages[i]);
                mailList.add(receiveMail);// 添加到邮件列表中
            }
            return mailList;
        }
    }
}
