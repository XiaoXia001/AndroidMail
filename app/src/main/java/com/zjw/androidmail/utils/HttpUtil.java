package com.zjw.androidmail.utils;

import com.zjw.androidmail.app.MyApplication;
import com.zjw.androidmail.bean.Attachment;
import com.zjw.androidmail.bean.MailInfo;
import com.zjw.androidmail.bean.MyAuthenticator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 * Created by zhaojinwei on 2017/4/4.
 */

public class HttpUtil {

    public Session login(){
        Session session = isLoginRight(MyApplication.info);
        return session;
    }

    public Session isLoginRight(MailInfo info){

        MyAuthenticator authenticator = null;
        if (info.isValidate()){
            authenticator = new MyAuthenticator(info.getUsername(), info.getPassword());
        }

        Session sendMailSession = Session.getDefaultInstance(info.getProperties(), authenticator);

        try {
            Transport transport = sendMailSession.getTransport("smtp");
            transport.connect(info.getMailServerHost(), info.getUsername(), info.getPassword());
        }catch (MessagingException e){
            e.printStackTrace();
            return null;
        }
        return sendMailSession;
    }

    public Boolean sendTextMail(MailInfo mailInfo, Session sendMailSession){

        try {
            Message mailMessage = new MimeMessage(sendMailSession);
            Address address = new InternetAddress(mailInfo.getFromAddress());
            mailMessage.setFrom(address);
            Address[] tos = null;
            String[] receivers = mailInfo.getReceivers();
            if (receivers != null){
                tos = new InternetAddress[receivers.length];
                for (int i = 0; i < receivers.length; i++){
                    tos[i] = new InternetAddress(receivers[i]);
                }
            }else {
                return false;
            }

            mailMessage.setRecipients(Message.RecipientType.TO, tos);
            mailMessage.setSubject(mailInfo.getSubject());
            mailMessage.setSentDate(new Date());

            String mailContent = mailInfo.getContent();

            Multipart mm = new MimeMultipart();
            BodyPart mdp = new MimeBodyPart();

            mdp.setContent(mailContent, "text/html;charset=gb2312");
            mm.addBodyPart(mdp);

            Attachment affInfos;
            FileDataSource fds1;
            List<Attachment> list = mailInfo.getAttachmentInfos();

            for (int i = 0; i < list.size(); i++){
                affInfos = list.get(i);
                fds1 = new FileDataSource(affInfos.getFilePath());
                mdp = new MimeBodyPart();
                mdp.setDataHandler(new DataHandler(fds1));
                try {
                    mdp.setFileName(MimeUtility.encodeText(fds1.getName()));
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                mm.addBodyPart(mdp);
            }
            mailMessage.setContent(mm);
            mailMessage.saveChanges();

            MailcapCommandMap mcm = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mcm.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handler.text_html");
            mcm.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handler.text_xml");
            mcm.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handler.text_plain");
            mcm.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handler.multipart_mixed");
            mcm.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handler.message_rfc822");
            CommandMap.setDefaultCommandMap(mcm);

            Transport.send(mailMessage);
            return true;
        }catch (MessagingException ex){
            ex.printStackTrace();
        }
        return false;
    }
}
