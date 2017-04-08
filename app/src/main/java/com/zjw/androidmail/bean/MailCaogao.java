package com.zjw.androidmail.bean;

/**
 * Created by zhaojinwei on 2017/4/7.
 */

public class MailCaogao {

    private int id;

    private String mailFrom;

    private String mailTo;

    private String subject;

    private String content;

    public MailCaogao(int id, String mailFrom, String mailTo, String subject, String content){
        super();
        this.id = id;
        this.mailFrom = mailFrom;
        this.mailTo = mailTo;
        this.subject = subject;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
