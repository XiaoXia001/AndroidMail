package com.zjw.androidmail.service;

import com.zjw.androidmail.utils.TranCharsetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

/**
 * Created by zhaojinwei on 2017/4/7.
 */

public class MailReceiver implements Serializable{

    private static final long serialVersionUID = 1L;

    private MimeMessage mimeMessage = null;

    private StringBuffer mailContent = new StringBuffer();

    private String dataFormat = "yyyy-MM-dd HH:mm:ss";

    private String charset;

    private boolean html;

    private ArrayList<String> attachments = new ArrayList<String>();

    private ArrayList<InputStream> attachmentsInputStreams = new ArrayList<InputStream>();

    public MailReceiver(MimeMessage mimeMessage){
        this.mimeMessage = mimeMessage;
        try {
            charset = parseCharset(mimeMessage.getContentType());
        }catch (MessagingException e){
            e.printStackTrace();
        }
    }

    public String getFrom() throws Exception{
        InternetAddress address[] = (InternetAddress[]) mimeMessage.getFrom();
        String addr = address[0].getAddress();
        String name = address[0].getPersonal();
        if (addr == null){
            addr = "";
        }
        if (name == null){
            name = "";
        }else if (charset == null){
            name = TranCharsetUtil.TranEncodeTOGB(name);
        }
        String nameAddr = name + "<" + addr + ">";
        return nameAddr;
    }

    public String getMailAddress(String type) throws Exception{
        String mailAddr = "";
        String addType = type.toUpperCase(Locale.CHINA);
        InternetAddress[] address = null;
        if (addType.equals("TO")){
            address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.TO);
        } else if (addType.equals("CC")) {
            address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.CC);
        } else if (addType.equals("BCC")) {
            address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.BCC);
        }else {
            System.out.println("Error type!");
            throw new Exception("Error emailaddress type!");
        }
        if (address != null){
            for (int i = 0; i < address.length; i++){
                String mailAddress = address[i].getAddress();
                if (mailAddress != null){
                    mailAddress = MimeUtility.decodeText(mailAddress);
                }else {
                    mailAddress = "";
                }

                String name = address[i].getPersonal();
                if (name != null) {
                    name = MimeUtility.decodeText(name);
                } else {
                    name = "";
                }

                mailAddr = name + "<" + mailAddress + ">";
            }
        }
        return mailAddr;
    }

    public String getSendDate() throws MessagingException {
        Date sendDate = mimeMessage.getSentDate();
        if (sendDate != null) {
            SimpleDateFormat format = new SimpleDateFormat(dataFormat, Locale.CHINA);
            return format.format(sendDate);
        } else {
            return "未知";
        }
    }

    public String getMailContent() throws Exception {
        compileMailContent((Part)mimeMessage);
        String content = mailContent.toString();
        if (content.indexOf("<html>") != -1) {
            html = true;
        }
        mailContent.setLength(0);
        return content;
    }

    public void setMailContent(StringBuffer mailContent) {
        this.mailContent = mailContent;
    }

    public String getSubject() {
        String subject = "";
        try {
            subject = mimeMessage.getSubject();
            if (subject.indexOf("=?gb18030?") != -1) {
                subject = subject.replace("gb18030", "gb2312");
            }
            subject = MimeUtility.decodeText(subject);
            if (charset == null) {
                subject = TranCharsetUtil.TranEncodeTOGB(subject);
            }
        } catch (Exception e) {
        }
        return subject;
    }

    public boolean getReplySign() throws MessagingException {
        boolean replySign = false;
        String needReply[] = mimeMessage.getHeader("Disposition-Notification-To");
        if (needReply != null) {
            replySign = true;
        }
        return replySign;
    }

    public boolean isNew() throws MessagingException {
        boolean isnew = false;
        Flags flags = ((Message) mimeMessage).getFlags();
        Flags.Flag[] flag = flags.getSystemFlags();
        for (int i = 0; i < flag.length; i++) {
            if (flag[i] == Flags.Flag.SEEN) {
                isnew = true;
                break;
            }
        }
        return isnew;
    }

    public String getMessageID() throws MessagingException {
        return mimeMessage.getMessageID();
    }

    public String getCharset() {
        return charset;
    }

    public ArrayList<String> getAttachments() {
        return attachments;
    }

    public boolean isHtml() {
        return html;
    }

    public ArrayList<InputStream> getAttachmentsInputStreams() {
        return attachmentsInputStreams;
    }

    private void compileMailContent(Part part) throws Exception {
        String contentType = part.getContentType();
        boolean connName = false;
        if (contentType.indexOf("name") != -1) {
            connName = true;
        }
        if (part.isMimeType("text/plain") && !connName) {
            //String content = parseInputStream((InputStream) part.getContent());
            //mailContent.append(content);
            mailContent.append((String) part.getContent());
        } else if (part.isMimeType("text/html") && !connName) {
            //html = true;
            //String content = parseInputStream((InputStream)part.getContent());
            //mailContent.append(content);
            mailContent.append((String) part.getContent());
        } else if (part.isMimeType("multipart/*") || part.isMimeType("message/rfc822")) {
            if (part.getContent() instanceof Multipart) {
                Multipart multipart = (Multipart) part.getContent();
                int counts = multipart.getCount();
                for (int i = 0; i < counts; i++) {
                    compileMailContent(multipart.getBodyPart(i));
                }
            } else {
                Multipart multipart = new MimeMultipart(new ByteArrayDataSource(part.getInputStream(), "multipart/*"));
                int counts = multipart.getCount();
                for (int i = 0; i < counts; i++) {
                    compileMailContent(multipart.getBodyPart(i));
                }
            }
        } else if (part.getDisposition() != null && part.getDisposition().equals(Part.ATTACHMENT)) {

            String filename = part.getFileName();
            if (filename != null) {
                if (filename.indexOf("=?gb18030?") != -1) {
                    filename = filename.replace("gb18030", "gb2312");
                }
                filename = MimeUtility.decodeText(filename);
                attachments.add(filename);
                attachmentsInputStreams.add(part.getInputStream());
            }
            // Log.e("content", "附件：" + filename);
        }
    }

    private String parseCharset(String contentType){
        if (!contentType.contains("charset")){
            return null;
        }
        if (contentType.contains("gbk")){
            return "GBK";
        }else if (contentType.contains("GB2312") || contentType.contains("gb18030")){
            return "gb2312";
        }else {
            String sub = contentType.substring(contentType.indexOf("charset") + 8).replace("\"", "");
            if (sub.contains(";")){
                return sub.substring(0, sub.indexOf(";"));
            }else {
                return sub;
            }
        }
    }

    private String parseInputStream(InputStream is) throws IOException, MessagingException {
        StringBuffer str = new StringBuffer();
        byte[] readByte = new byte[1024];
        int count;
        try {
            while ((count = is.read(readByte)) != -1) {
                if (charset == null) {
                    str.append(new String(readByte, 0, count, "GBK"));
                } else {
                    str.append(new String(readByte, 0, count, charset));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str.toString();
    }
}
