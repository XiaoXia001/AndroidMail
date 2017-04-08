package com.zjw.androidmail;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.zjw.androidmail.app.MyApplication;
import com.zjw.androidmail.bean.MailUsers;

public class MailAddContact extends AppCompatActivity {

    private ListView lv;

    private MyAdapter adapter;

    private List<MailUsers> list;

    private List<String> chooseUsers = new ArrayList<String>();

    private Uri uri = Uri.parse("content://com.zjw.mailconstantprovider");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_add_contact);

        list = getAllConstacts();

        lv = (ListView) findViewById(R.id.show_contact);
        adapter = new MyAdapter();
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MailUsers user = (MailUsers) parent.getItemAtPosition(position);
                CheckBox ckBox = (CheckBox) view.findViewById(R.id.ck_box);
                if (chooseUsers.contains(user.getAddress())){
                    chooseUsers.remove(user.getAddress());
                    ckBox.setChecked(false);
                }else {
                    chooseUsers.add(user.getAddress());
                    ckBox.setChecked(true);
                }
            }
        });
    }

    private List<MailUsers> getAllConstacts(){
        List<MailUsers> users=new ArrayList<MailUsers>();
        Cursor c=getContentResolver().query(uri, null, "mailfrom=?", new String[]{MyApplication.info.getUsername()}, null);
        while(c.moveToNext()){
            MailUsers user=new MailUsers(c.getInt(0), c.getString(2), c.getString(3));
            users.add(user);
        }
        return users;
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View item=View.inflate(MailAddContact.this, R.layout.mail_add_contact_item, null);
            TextView name=(TextView) item.findViewById(R.id.add_name);
            TextView address=(TextView) item.findViewById(R.id.add_address);
            CheckBox ck_box=(CheckBox) item.findViewById(R.id.ck_box);

            MailUsers user=list.get(position);
            name.setText(user.getName());
            address.setText(user.getAddress());

            if(chooseUsers.contains(user.getAddress())){
                ck_box.setChecked(true);
            }else{
                ck_box.setChecked(false);
            }
            return item;
        }

    }

    public void back(View view){
        Intent data = new Intent();
        chooseUsers.clear();
        data.putStringArrayListExtra("chooseUsers", (ArrayList<String>) chooseUsers);
        setResult(2, data);
        finish();
    }

    public void choose(View view){
        Intent data = new Intent();
        data.putStringArrayListExtra("chooseUsers", (ArrayList<String>) chooseUsers);
        setResult(2, data);
        finish();
    }
}
