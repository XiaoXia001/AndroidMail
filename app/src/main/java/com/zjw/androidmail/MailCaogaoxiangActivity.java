package com.zjw.androidmail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.zjw.androidmail.app.MyApplication;
import com.zjw.androidmail.bean.MailCaogao;

import java.util.ArrayList;
import java.util.List;

public class MailCaogaoxiangActivity extends AppCompatActivity {

    private ListView lv;

    private List<MailCaogao> all;

    private MyAdapter adapter;

    private Uri uri = Uri.parse("content://com.zjw.caogaoxiangprovider");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_caogaoxiang);
        all = getAllCaogao();
        lv = (ListView) findViewById(R.id.caogaoxiang);
        adapter = new MyAdapter();
        lv.setAdapter(adapter);


        adapter.notifyDataSetChanged();
    }

    private List<MailCaogao> getAllCaogao(){
        List<MailCaogao> caogaos = new ArrayList<MailCaogao>();
        Cursor cursor = getContentResolver().query(uri, null, "mailfrom=?", new String[]{MyApplication.info.getUsername()},null);
        while (cursor.moveToNext()){
            MailCaogao caogao = new MailCaogao(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            caogaos.add(caogao);
        }
        cursor.close();
        return caogaos;
    }

    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount(){
            return all.size();
        }

        @Override
        public Object getItem(int position){
            return all.get(position);
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            View item = View.inflate(MailCaogaoxiangActivity.this, R.layout.mail_caogaoxiang_item, null);

            TextView mailTo = (TextView) item.findViewById(R.id.tv_mailto);
            TextView mailSubject = (TextView) item.findViewById(R.id.tv_mailsubject);
            ImageButton delete = (ImageButton) item.findViewById(R.id.delete1);

            MailCaogao caogao = all.get(position);
            mailTo.setText(caogao.getMailTo());
            mailSubject.setText(caogao.getSubject());

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(MailCaogaoxiangActivity.this)
                            .setTitle("编辑或者删除")
                            .setPositiveButton("编辑", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MailCaogao caogao = all.get(position);
                                    Intent intent = new Intent(MailCaogaoxiangActivity.this, MailEditActivity.class);
                                    intent.putExtra("mailid", caogao.getId());
                                    //intent.putExtra("iscaogaoxiang", true);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MailCaogao caogao = all.get(position);
                                    Uri uri = Uri.parse("content://com.zjw.caogaoxiangprovider");
                                    getContentResolver().delete(uri, "id=?", new String[]{caogao.getId() + ""});
                                    all.remove(position);
                                    notifyDataSetChanged();
                                }
                            }).show();
                }
            });

            return item;
        }
    }

    public void back(View view){
        finish();
    }
}
