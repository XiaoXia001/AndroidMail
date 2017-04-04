package com.zjw.androidmail.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

/**
 * Created by zhaojinwei on 2017/4/4.
 */

public class MailInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String mailServerHost;
    private String mailServerPort = "25";

    private String username;
    private String password;

    private Boolean validate = false;

    private String fromAddress;
    private String subject;
    private String content;
    private String[] receivers;

    private List<Attachment> attachmentInfos;

    public Properties getProperties(){
        Properties p = new Properties();
        p.put("mail.smtp.host", this.mailServerHost);
        p.put("mail.smtp.port", this.mailServerPort);
        p.put("mail.smtp.auth", validate ? "true" : "false");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        p.put("mail.smtp.socketFactory.port", "465");
        return p;
    }

    public String[] getReceivers(){
        return receivers;
    }

    public void setReceivers(String[] receivers){
        this.receivers = receivers;
    }

    public String getMailServerHost(){
        return mailServerHost;
    }

    public void setMailServerHost(String mailServerHost){
        this.mailServerHost = mailServerHost;
    }

    public String getMailServerPort(){
        return mailServerPort;
    }

    public void setMailServerPort(String mailServerPort){
        this.mailServerPort = mailServerPort;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public Boolean isValidate(){
        return validate;
    }

    public void setValidate(Boolean validate){
        this.validate = validate;
    }

    public String getFromAddress(){
        return fromAddress;
    }

    public void setFromAddress(String fromAddress){
        this.fromAddress = fromAddress;
    }


    public String getSubject(){
        return subject;
    }

    public void setSubject(String subject){
        this.subject = subject;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content){
        this.content = content;
    }

    public List<Attachment> getAttachmentInfos(){
        return attachmentInfos;
    }

    public void setAttachmentInfos(List<Attachment> attachmentInfos){
        this.attachmentInfos = attachmentInfos;
    }
}
