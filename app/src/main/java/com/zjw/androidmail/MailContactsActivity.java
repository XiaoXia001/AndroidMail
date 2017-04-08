package com.zjw.androidmail;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.AlertDialog.Builder;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zjw.androidmail.app.MyApplication;
import com.zjw.androidmail.bean.MailUsers;

import java.util.ArrayList;
import java.util.List;

public class MailContactsActivity extends AppCompatActivity {

    private ListView lv;

    private List<MailUsers> list;

    private ProgressDialog dialog;

    private Uri uri = Uri.parse("content://com.zjw.mailconstantprovider");

    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_contacts);

        dialog = new ProgressDialog(this);
        dialog.setMessage("正加载");
        dialog.show();

        list = getAllConstants();
        init();

        dialog.dismiss();
        registerForContextMenu(lv);
    }

    private List<MailUsers> getAllConstants(){
        List<MailUsers> users = new ArrayList<MailUsers>();
        Cursor cursor = getContentResolver().query(uri, null, "mailfrom=?", new String[]{MyApplication.info.getUsername()}, null);
        while (cursor.moveToNext()){
            MailUsers user = new MailUsers(cursor.getInt(0), cursor.getString(2), cursor.getString(3));
            users.add(user);
        }
        return users;
    }

    private void init(){
        lv = (ListView) findViewById(R.id.lv_constant);

        adapter = new MyAdapter();
        lv.setAdapter(adapter);
    }

    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount(){
            return list.size();
        }

        @Override
        public Object getItem(int position){
            return list.get(position);
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View view =  View.inflate(MailContactsActivity.this, R.layout.mail_contacts_item, null);
            TextView name = (TextView) view.findViewById(R.id.tv_name);
            TextView address = (TextView) view.findViewById(R.id.tv_address);

            MailUsers user = list.get(position);
            name.setText(user.getName());
            address.setText(user.getAddress());
            return view;
        }
    }

    public void back(View view){
        finish();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        getMenuInflater().inflate(R.menu.constants_menu, menu);
        super.onCreateContextMenu(menu, view, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int id = (int) info.id;
        switch (item.getItemId()){
            case R.id.update:
                updateAddress(list.get(id).getName(), list.get(id).getAddress());
                break;
            case R.id.delete:
                deleteAddress(list.get(id).getName(), list.get(id).getAddress());
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void updateAddress(final String name, String address){
        Builder builder = new Builder(MailContactsActivity.this);
        builder.setTitle("修改邮箱地址");
        final EditText editText = new EditText(MailContactsActivity.this);
        editText.setText(address);
        builder.setView(editText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContentValues values = new ContentValues();
                values.put("address", editText.getText().toString().trim());
                getContentResolver().update(uri, values, "mailfrom=? and name=?", new String[]{MyApplication.info.getUsername(), name});

                list = getAllConstants();
                adapter.notifyDataSetChanged();
                Toast.makeText(MailContactsActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void deleteAddress(final String name, String address){
        Builder builder = new Builder(MailContactsActivity.this);
        builder.setMessage("你确定要删除数据");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getContentResolver().delete(uri, "mailfrom=? and name=?", new String[]{MyApplication.info.getUsername(), name});

                list = getAllConstants();
                adapter.notifyDataSetChanged();
                Toast.makeText(MailContactsActivity.this, "删除数据成功", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
}
