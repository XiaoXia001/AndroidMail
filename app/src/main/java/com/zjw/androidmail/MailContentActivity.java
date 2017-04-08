package com.zjw.androidmail;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zjw.androidmail.app.MyApplication;
import com.zjw.androidmail.bean.Mail;
import com.zjw.androidmail.utils.IOUtil;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MailContentActivity extends AppCompatActivity {

    private TextView tvAddr, tvMailSubject, tvMailContent;

    private ListView lvMailAttachment;

    private WebView wvMailContent;

    private Button btnCancel, btnRelay;

    private ArrayList<InputStream> attachmentsInputStreams;

    private Mail mail;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_content);
        mail = (Mail) getIntent().getSerializableExtra("MAIL");
        attachmentsInputStreams = ((MyApplication) getApplication()).getAttachmentInputStream();
        init();
    }

    private void init(){
        handler = new MyHandler(this);
        tvAddr = (TextView) findViewById(R.id.tv_addr);
        tvMailSubject = (TextView) findViewById(R.id.tv_mailsubject);
        tvMailContent = (TextView) findViewById(R.id.tv_mailcontent);
        if (mail.getAttachments().size() > 0){
            lvMailAttachment = (ListView) findViewById(R.id.lv_mailattachment);
            lvMailAttachment.setVisibility(View.VISIBLE);
            lvMailAttachment.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mail.getAttachments()));
            lvMailAttachment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            handler.obtainMessage(0, "开始下载\"" + mail.getAttachments().get(position) + "\"").sendToTarget();
                            InputStream inputStream = attachmentsInputStreams.get(position);
                            String path = new IOUtil().stream2file(inputStream, Environment.getExternalStorageDirectory().toString() + "/temp/" + mail.getAttachments().get(position));
                            if (path == null){
                                handler.obtainMessage(0, "下载失败").sendToTarget();
                            }else {
                                handler.obtainMessage(0, "文件保存在：" + path).sendToTarget();
                            }
                        }
                    }).start();
                }
            });

        }

        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnRelay = (Button) findViewById(R.id.btn_relay);

        tvAddr.setText(mail.getFrom());
        tvMailSubject.setText(mail.getSubject());
        if (mail.isHtml()){
            wvMailContent = (WebView) findViewById(R.id.wv_mailcontent);
            wvMailContent.setVisibility(View.VISIBLE);
            wvMailContent.loadDataWithBaseURL(null, mail.getContent(), "text/html", "utf-8", null);
            wvMailContent.getSettings().setBuiltInZoomControls(true);

            DisplayMetrics dm = getResources().getDisplayMetrics();
            int scale = dm.densityDpi;
            if (scale == 240) {
                wvMailContent.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
            } else if (scale == 160) {
                wvMailContent.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
            } else {
                wvMailContent.getSettings().setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
            }
            wvMailContent.setWebChromeClient(new WebChromeClient());
            tvMailContent.setVisibility(View.GONE);
        }else {
            tvMailContent.setText(mail.getContent());
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MailContentActivity.this.finish();
            }
        });

        btnRelay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MailContentActivity.this, MailEditActivity.class).putExtra("MAIL", mail).putExtra("TYPE", 1));
            }
        });
    }

    private static class MyHandler extends Handler{

        private WeakReference<MailContentActivity> wrActivity;

        public MyHandler(MailContentActivity activity){
            this.wrActivity = new WeakReference<MailContentActivity>(activity);
        }

        @Override
        public void handleMessage(Message message){
            final MailContentActivity activity = wrActivity.get();
            switch (message.what){
                case 0:
                    Toast.makeText(activity.getApplicationContext(), message.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }
}
