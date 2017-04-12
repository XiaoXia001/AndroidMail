package com.zjw.androidmail.service;

import android.util.Base64;

import com.sun.mail.imap.protocol.BASE64MailboxDecoder;
import com.sun.mail.util.BASE64DecoderStream;
import com.zjw.androidmail.utils.TranCharsetUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.mail.BodyPart;
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

    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    private String charset;

    private boolean html;

    private ArrayList<String> attachments = new ArrayList<String>();

    private ArrayList<InputStream> attachmentsInputStreams = new ArrayList<InputStream>();

    private String saveAttachPath = "d:\\";

    private StringBuffer bodyText = new StringBuffer();

    public MailReceiver(){

    }

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
        //SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMddHHmmss");
        if (sendDate != null) {
            SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.CHINA);
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

    public String getBodyText(){
        return bodyText.toString();
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

    public boolean isContainAttachment(Part part) throws Exception{
        boolean attachFlag = false;
        String contentType = part.getContentType();
        if (part.isMimeType("multipart/*")){
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++){
                BodyPart mPart = mp.getBodyPart(i);
                String disposition = mPart.getDisposition();
                if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT))
                || (disposition.equals(Part.INLINE)))) {
                    attachFlag = true;
                }else if (mPart.isMimeType("multipart/*")){
                    attachFlag = isContainAttachment((Part) mPart);
                }else {
                    String conType = mPart.getContentType();
                    if ((conType.toLowerCase().indexOf("application") != -1)
                    || (conType.toLowerCase().indexOf("name") != -1)){
                        attachFlag = true;
                    }
                }
            }
        }else if (part.isMimeType("message/rfc822")){
            attachFlag = isContainAttachment((Part) part.getContent());
        }
        return attachFlag;
    }

    private static String base64Decoder(String s) throws Exception{
        byte[] b = Base64.decode(s, Base64.DEFAULT);
        return (new String(b));
    }

    public String saveAttachment(Part part) throws Exception{
        String fileName = "";
        if (part.isMimeType("multipart/*")){
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++){
                BodyPart mPart = mp.getBodyPart(i);
                String disposition = mPart.getDisposition();
                if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT))
                        || (disposition.equals(Part.INLINE)))) {
                    fileName = mPart.getFileName();
                    String s = fileName.substring(8, fileName.indexOf("?="));
                    fileName = base64Decoder(s);
                    if (fileName.toLowerCase().indexOf("gb2312") != -1){
                        fileName = MimeUtility.decodeText(fileName);
                    }
                    saveFile(fileName, mPart.getInputStream());
                }else if (mPart.isMimeType("multipart/*")){
                    saveAttachment(mPart);
                }else {
                    fileName = mPart.getFileName();
                    if ((fileName != null) && (fileName.indexOf("GB2312") != -1)){
                        fileName = MimeUtility.decodeText(fileName);
                        saveFile(fileName, mPart.getInputStream());
                    }
                }
            }
        }else if (part.isMimeType("message/rfc822")){
            saveAttachment((Part) part.getContent());
        }
        return fileName;
    }

    public void setAttachPath(String attachPath){
        this.saveAttachPath = attachPath;
    }

    public String getAttachPath(){
        return saveAttachPath;
    }

    private void saveFile(String fileName, InputStream in) throws Exception{
        String osName = System.getProperty("os.name");
        String storeDir = getAttachPath();
        String separator = "";
        if (osName == null){
            osName = "";
        }
        if (osName.toLowerCase().indexOf("win") != -1){
            separator = "\\";
            if (storeDir == null || storeDir.equals("")){
                storeDir = "c:\\tmp";
            }
        }else {
            separator = "/";
            storeDir = "/tmp";
        }
        File storeFile = new File(storeDir + separator + fileName);
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(storeFile));
            bis = new BufferedInputStream(in);
            int c;
            while ((c = bis.read()) != -1){
                bos.write(c);
                bos.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("文件保存失败！");
        }finally {
            bos.close();
            bis.close();
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
