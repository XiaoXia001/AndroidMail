package com.zjw.androidmail;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zjw.androidmail.app.MyApplication;
import com.zjw.androidmail.bean.Mail;
import com.zjw.androidmail.service.MailHelper;
import com.zjw.androidmail.service.MailReceiver;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import javax.mail.MessagingException;

public class MailBoxActivity extends AppCompatActivity {

    private ArrayList<Mail> mailList = new ArrayList<Mail>();

    private ArrayList<ArrayList<InputStream>> attachmentsInputStreamsList = new ArrayList<ArrayList<InputStream>>();

    private String type;

    private int status;

    private MyAdapter myAdapter;

    private ListView lvBox;

    private List<MailReceiver> mailReceivers;

    private ProgressDialog dialog;

    private Uri uri = Uri.parse("content://com.zjw.mailstatusprovider");

    private List<String> messageids;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message message){
            switch (message.what){
                case 0:
                    myAdapter.notifyDataSetChanged();
                    //lvBox = (ListView) findViewById(R.id.lv_box);
                    //myAdapter = new MyAdapter();
                    //lvBox.setAdapter(myAdapter);
                    break;
                case 1:
                    dialog.dismiss();
                    Toast.makeText(MailBoxActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MailBoxActivity.this, MainActivity.class);
                    startActivity(intent);
                case 2:
                    dialog.dismiss();
                    break;
            }
            super.handleMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getStringExtra("TYPE");
        status = getIntent().getIntExtra("status", -1);
        setContentView(R.layout.mail_box);
        initView();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mailReceivers = MailHelper.getInstance(MailBoxActivity.this).getAllMail(type);
                }catch (MessagingException e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(1);
                }
                messageids = getAllMessageids();

                switch (status){
                    case 0:
                        getAllMails(mailReceivers);
                        break;
                    case 1:
                        getNotRead(mailReceivers);
                        break;
                    case 2:
                        getHasRead(mailReceivers);
                        break;
                }
                handler.sendEmptyMessage(2);
            }
        }).start();
    }

    private void initView(){
        lvBox = (ListView) findViewById(R.id.lv_box);
        myAdapter = new MyAdapter();
        lvBox.setAdapter(myAdapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("正加载");
        dialog.show();


        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

    private List<String> getAllMessageids(){
        List<String> messageids=new ArrayList<String>();
        Cursor c=getContentResolver().query(uri, null, "mailfrom=?", new String[]{MyApplication.info.getUsername()}, null);
        while(c.moveToNext()){
            messageids.add(c.getString(2));
        }
        c.close();
        return messageids;
    }

    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mailList.size();
        }

        @Override
        public Object getItem(int position) {
            return mailList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(MailBoxActivity.this).inflate(R.layout.mail_box_item, null);
            TextView tvFrom = (TextView) convertView.findViewById(R.id.tv_from);
            tvFrom.setText(mailList.get(position).getFrom());
            TextView tvSendData = (TextView) convertView.findViewById(R.id.tv_send);
            tvSendData.setText(mailList.get(position).getSendData());
            if (mailList.get(position).isNews()){
                TextView tvNew = (TextView) convertView.findViewById(R.id.tv_new);
                tvNew.setVisibility(View.VISIBLE);
            }
            TextView tvSubject = (TextView) convertView.findViewById(R.id.tv_subject);
            tvSubject.setText(mailList.get(position).getSubject());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String mailID = mailList.get(position).getMessageID();
                    if (!messageids.contains(mailID)){
                        ContentValues values = new ContentValues();
                        values.put("mailfrom", MyApplication.info.getUsername());
                        values.put("messageid", mailID);
                        getContentResolver().insert(uri, values);
                    }

                    ((MyApplication)getApplication()).setAttachmentInputStream(attachmentsInputStreamsList.get(position));
                    final Intent intent = new Intent(MailBoxActivity.this, MailContentActivity.class).putExtra("MAIL", mailList.get(position));
                    startActivity(intent);
                }
            });
            return convertView;
        }

    }

    private void getAllMails(List<MailReceiver> mails){
        for (MailReceiver mailReceiver : mails){
            Mail mail = new Mail();
            try {
                mail.setMessageID(mailReceiver.getMessageID());
                mail.setFrom(mailReceiver.getFrom());
                mail.setTo(mailReceiver.getMailAddress("TO"));
                mail.setCc(mailReceiver.getMailAddress("CC"));
                mail.setBcc(mailReceiver.getMailAddress("BCC"));
                mail.setSubject(mailReceiver.getSubject());
                mail.setSendData(mailReceiver.getSendDate());
                mail.setContent(mailReceiver.getMailContent());
                mail.setReplySign(mailReceiver.getReplySign());
                mail.setHtml(mailReceiver.isHtml());
                mail.setNews(mailReceiver.isNew());
                mail.setCharset(mailReceiver.getCharset());
                attachmentsInputStreamsList.add(0, mailReceiver.getAttachmentsInputStreams());
                mailList.add(0, mail);
                handler.sendEmptyMessage(0);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void getHasRead(List<MailReceiver> mails){
        for (MailReceiver mailReceiver : mails){
            Mail mail = new Mail();
            try {
                if (messageids.contains(mailReceiver.getMessageID())){
                    continue;
                }
                mail.setMessageID(mailReceiver.getMessageID());
                mail.setFrom(mailReceiver.getFrom());
                mail.setTo(mailReceiver.getMailAddress("TO"));
                mail.setCc(mailReceiver.getMailAddress("CC"));
                mail.setBcc(mailReceiver.getMailAddress("BCC"));
                mail.setSubject(mailReceiver.getSubject());
                mail.setSendData(mailReceiver.getSendDate());
                mail.setContent(mailReceiver.getMailContent());
                mail.setReplySign(mailReceiver.getReplySign());
                mail.setHtml(mailReceiver.isHtml());
                mail.setNews(mailReceiver.isNew());
                mail.setCharset(mailReceiver.getCharset());
                attachmentsInputStreamsList.add(0, mailReceiver.getAttachmentsInputStreams());
                mailList.add(0, mail);
                handler.sendEmptyMessage(0);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void getNotRead(List<MailReceiver> mails){
        for (MailReceiver mailReceiver : mails){
            Mail mail = new Mail();
            try {
                if (messageids.contains(mailReceiver.getMessageID())) {
                    mail.setMessageID(mailReceiver.getMessageID());
                    mail.setFrom(mailReceiver.getFrom());
                    mail.setTo(mailReceiver.getMailAddress("TO"));
                    mail.setCc(mailReceiver.getMailAddress("CC"));
                    mail.setBcc(mailReceiver.getMailAddress("BCC"));
                    mail.setSubject(mailReceiver.getSubject());
                    mail.setSendData(mailReceiver.getSendDate());
                    mail.setContent(mailReceiver.getMailContent());
                    mail.setReplySign(mailReceiver.getReplySign());
                    mail.setHtml(mailReceiver.isHtml());
                    mail.setNews(mailReceiver.isNew());
                    mail.setCharset(mailReceiver.getCharset());
                    attachmentsInputStreamsList.add(0, mailReceiver.getAttachmentsInputStreams());
                    mailList.add(0, mail);
                    handler.sendEmptyMessage(0);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void back(View view){
        finish();
    }
}
