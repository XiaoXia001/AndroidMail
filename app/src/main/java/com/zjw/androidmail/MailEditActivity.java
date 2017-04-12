package com.zjw.androidmail;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zjw.androidmail.adapter.GridViewAdapter;
import com.zjw.androidmail.app.MyApplication;
import com.zjw.androidmail.bean.Attachment;
import com.zjw.androidmail.utils.HttpUtil;

import java.util.ArrayList;
import java.util.List;

public class MailEditActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mailTo;

    private EditText mailFrom;

    private EditText mailTopic;

    private EditText mailContent;

    private Button send;

    private ImageButton addContact;

    private ImageButton attachment;

    private GridView gridView;

    private GridViewAdapter<Attachment> adapter = null;

    private int mailId = -1;

    private static final int SUCCESS = 1;

    private static final int FAILED = -1;

    private boolean isCaogaoxiang = true;

    private ProgressDialog dialog;

    HttpUtil util = new HttpUtil();

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message message){
            switch (message.what){
                case SUCCESS:
                    dialog.cancel();
                    isCaogaoxiang = false;
                    if (mailId > 0){
                        Uri uri = Uri.parse("content://com.zjw.caogaoxiangprovider");
                        getContentResolver().delete(uri, "id=?", new String[]{mailId + ""});
                        uri = Uri.parse("content://com.zjw.attachmentprovider");
                        getContentResolver().delete(uri, "mailid=?", new String[]{mailId + ""});
                        Toast.makeText(getApplicationContext(), "邮件发送成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MailEditActivity.this, MailCaogaoxiangActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Toast.makeText(getApplicationContext(), "邮件发送成功", Toast.LENGTH_SHORT).show();
                        /*mailFrom.getText().clear();
                        mailTo.getText().clear();
                        mailTopic.getText().clear();
                        mailContent.getText().clear();
                        adapter = new GridViewAdapter<Attachment>(MailEditActivity.this);*/
                        Intent intent = new Intent(MailEditActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    break;
                case FAILED:
                    dialog.cancel();
                    isCaogaoxiang = true;
                    Toast.makeText(getApplicationContext(), "邮件发送失败", Toast.LENGTH_SHORT);
                    break;
            }
            super.handleMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_edit);

        mailTo = (EditText) findViewById(R.id.mail_to);
        mailFrom = (EditText) findViewById(R.id.mail_from);
        mailTopic = (EditText) findViewById(R.id.mail_topic);
        mailContent = (EditText) findViewById(R.id.content);
        send = (Button) findViewById(R.id.send);
        attachment = (ImageButton) findViewById(R.id.add_add);
        addContact = (ImageButton) findViewById(R.id.add_contact);
        gridView = (GridView) findViewById(R.id.pre_view);

        mailFrom.setText(MyApplication.info.getUsername());
        send.setOnClickListener(this);
        attachment.setOnClickListener(this);
        addContact.setOnClickListener(this);

        adapter = new GridViewAdapter<Attachment>(this);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new MyOnItemClickListener());

        mailId = getIntent().getIntExtra("mailid", -1);
        if (mailId > -1){
            Uri uri = Uri.parse("content://com.zjw.caogaoxiangprovider");
            Cursor cursor = getContentResolver().query(uri, null, "mailfrom=? and id=?", new String[]{MyApplication.info.getUsername(), mailId + ""}, null);
            if (cursor.moveToNext()){
                mailTo.setText(cursor.getString(2));
                mailTopic.setText(cursor.getString(3));
                mailContent.setText(cursor.getString(4));
            }

            uri = Uri.parse("content://com.zjw.attachmentprovider");
            cursor = getContentResolver().query(uri, null, "mailid=?", new String[]{mailId + ""}, null);
            List<Attachment> attachments = new ArrayList<Attachment>();
            while (cursor.moveToNext()){
                Attachment att = new Attachment(cursor.getString(2), cursor.getString(1), cursor.getLong(3));
                attachments.add(att);
            }

            if (attachments.size() > 0){
                for (Attachment affInfos : attachments){
                    adapter.appendToList(affInfos);
                    int a = adapter.getList().size();
                    int count = (int) Math.ceil(a / 4.0);
                    gridView.setLayoutParams(new ActionBar.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, (int) (94 * 1.5 * count)
                    ));
                }
            }
        }
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.send:
                sendMail();
                break;
            case R.id.add_add:
                addAttachment();
                break;
            case R.id.add_contact:
                Intent intent = new Intent(MailEditActivity.this, MailAddContact.class);
                startActivityForResult(intent, 2);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    Uri uri = null;
                    if (data != null) {
                        uri = data.getData();
                    }

                    String path = uri.getPath();
                    Attachment affInfos = Attachment.getFileInfo(path);
                    adapter.appendToList(affInfos);
                    int a = adapter.getList().size();
                    int count = (int) Math.ceil(a / 4.0);
                    gridView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            (int) (104 * 1.5 * count)));
                    break;
            }
        }

        if(requestCode == 2){
            List<String> chooseUsers = data.getStringArrayListExtra("chooseUsers");
            StringBuilder str = new StringBuilder();
            for(int i = 0; i < chooseUsers.size(); i++){
                if(i == chooseUsers.size() - 1){
                    str.append("<" + chooseUsers.get(i) + ">");
                }else{
                    str.append("<" + chooseUsers.get(i) + ">,");
                }
            }
            mailTo.setText(str.toString());

        }
    }

    private void sendMail(){
        MyApplication.info.setAttachmentInfos(adapter.getList());
        MyApplication.info.setFromAddress(mailFrom.getText().toString().trim());
        MyApplication.info.setSubject(mailTopic.getText().toString().trim());
        MyApplication.info.setContent(mailContent.getText().toString().trim());

        String str = mailTo.getText().toString().trim();
        String[] receivers = str.split(",");
        for (int i = 0; i < receivers.length; i++){
            if (receivers[i].startsWith("<") && receivers[i].endsWith(">")){
                receivers[i] = receivers[i].substring(receivers[i].lastIndexOf("<") + 1, receivers[i].lastIndexOf(">"));
            }
        }
        MyApplication.info.setReceivers(receivers);

        dialog = new ProgressDialog(this);
        dialog.setMessage("正在发送");
        dialog.show();

        new Thread(){
            @Override
            public void run(){
                boolean flag = util.sendTextMail(MyApplication.info, MyApplication.session);
                Message message = new Message();
                if (flag){
                    message.what = SUCCESS;
                    handler.sendMessage(message);
                }else {
                    message.what = FAILED;
                    handler.sendMessage(message);
                }
            }
        }.start();
    }

    private void addAttachment(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/");
        startActivityForResult(intent, 1);
    }

    private class MyOnItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3){
            Attachment infos = (Attachment) adapter.getItem(arg2);
            Builder builder = new Builder(MailEditActivity.this);
            builder.setTitle(infos.getFileName());
            builder.setIcon(getResources().getColor(android.R.color.transparent));
            builder.setMessage("是否删除当前附件");
            builder.setNegativeButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.clearPositionList(arg2);
                            int a = adapter.getList().size();
                            int count = (int) Math.ceil(a / 4.0);
                            gridView.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, (int) (104 * 1.5 * count)
                            ));
                        }
                    });
            builder.setPositiveButton("取消", null);
            builder.create().show();
        }
    }

    public void back(View view){
        if (!mailTopic.getText().toString().trim().equals("") || !mailContent.getText().toString().trim().equals("")){
            Builder builder = new Builder(MailEditActivity.this);
            builder.setMessage("是否存入草稿箱");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveToCaogaoxiang();
                    finish();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.show();
        }else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(isCaogaoxiang&&mailTo.getText().toString().trim()!=null){
                Builder builder=new Builder(MailEditActivity.this);
                builder.setMessage("是否存入草稿箱");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //保存至数据库
                        saveToCaogaoxiang();
                    }

                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                });
                builder.show();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void saveToCaogaoxiang(){
        Uri uri = Uri.parse("content://com.zjw.caogaoxiangprovider");
        ContentValues values = new ContentValues();
        values.put("mailfrom", MyApplication.info.getUsername());
        values.put("mailto", mailTo.getText().toString().trim());
        values.put("subject", mailTopic.getText().toString().trim());
        values.put("content", mailContent.getText().toString().trim());

        String url = getContentResolver().insert(uri, values).toString();
        int id = Integer.parseInt(url.substring(url.length() - 1));

        if (adapter.getList().size() > 0){
            Uri att_uri = Uri.parse("content://com.zjw.attachmentprovider");
            List<Attachment> attachments = adapter.getmList();
            values.clear();
            for (int i = 0; i < attachments.size(); i++){
                Attachment att = attachments.get(i);
                values.put("filename", att.getFileName());
                values.put("filepath", att.getFilePath());
                values.put("filesize", att.getFileSize());
                values.put("mailid", id);
                getContentResolver().insert(att_uri, values);
            }
        }
        Toast.makeText(MailEditActivity.this, "保存至草稿箱", Toast.LENGTH_SHORT).show();
    }
}
