package com.zjw.androidmail;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zjw.androidmail.app.MyApplication;
import com.zjw.androidmail.utils.EmailFormatUtil;

public class MainActivity extends AppCompatActivity {

    private ExpandableListView expandableListView;

    private int[] group_click = new int[5];

    private long mExitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        final MyExpandAdapter adapter = new MyExpandAdapter();

        expandableListView = (ExpandableListView) findViewById(R.id.list);
        expandableListView.setGroupIndicator(null);
        expandableListView.setAdapter(adapter);

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                group_click[groupPosition]++;
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener(){

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id){

                if (groupPosition == 0 && childPosition == 0){
                    Intent intent = new Intent(MainActivity.this, MailContactsActivity.class);
                    startActivity(intent);
                }else if (groupPosition == 0 && childPosition == 1){
                    Builder builder = new Builder(MainActivity.this);
                    builder.setTitle("添加联系人");

                    View view = getLayoutInflater().inflate(R.layout.mail_add_address, null);

                    final EditText name = (EditText) view.findViewById(R.id.name);
                    final EditText address = (EditText) view.findViewById(R.id.address);

                    builder.setView(view);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            insertAddress(name.getText().toString().trim(), address.getText().toString().trim());
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                }else if (groupPosition == 1 && childPosition == 0){
                    Intent intent = new Intent(MainActivity.this, MailEditActivity.class);
                    startActivity(intent);
                }else if (groupPosition == 1 && childPosition == 1){
                    Intent intent = new Intent(MainActivity.this, MailCaogaoxiangActivity.class);
                    startActivity(intent);
                }else if (groupPosition == 2 && childPosition == 0){
                    Intent intent = new Intent(MainActivity.this, MailBoxActivity.class);
                    intent.putExtra("TYPE", "INBOX");
                    intent.putExtra("status", 0);
                    startActivity(intent);
                }else if (groupPosition == 2 && childPosition == 1){
                    Intent intent = new Intent(MainActivity.this, MailBoxActivity.class);
                    intent.putExtra("TYPE", "INBOX");
                    intent.putExtra("status", 1);
                    startActivity(intent);
                }else if (groupPosition == 2 && childPosition == 2){
                    Intent intent = new Intent(MainActivity.this, MailBoxActivity.class);
                    intent.putExtra("TYPE", "INBOX");
                    intent.putExtra("status", 2);
                    startActivity(intent);
                }

                adapter.notifyDataSetChanged();
                return false;
            }
        });
    }

    private class MyExpandAdapter extends BaseExpandableListAdapter{

        String[] group_title = new String[]{"联系人", "写邮件", "收件箱"};

        String[][] child_text = new String[][]{
                {"联系人列表", "添加联系人"},
                {"新邮件", "草稿箱"},
                {"全部邮件", "未读邮件", "已读邮件"}
        };

        int[][] child_icons = new int[][]{
                {R.drawable.contact_list, R.drawable.add_contacts},
                {R.drawable.new_mail, R.drawable.caogaoxiang},
                {R.drawable.all_mail, R.drawable.not_read, R.drawable.has_read}
        };

        @Override
        public Object getChild(int groupPosition, int childPosition){
            return child_text[groupPosition][childPosition];
        }

        @Override
        public long getChildId(int groupPosition, int childPosition){
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView,
                                 ViewGroup parent){
            convertView = getLayoutInflater().inflate(R.layout.mail_child, null);
            TextView textView = (TextView) convertView.findViewById(R.id.text_view);
            textView.setText(child_text[groupPosition][childPosition]);

            ImageView childIcon= (ImageView) convertView.findViewById(R.id.child_icon);
            childIcon.setImageResource(child_icons[groupPosition][childPosition]);

            return  convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition){
            return child_text[groupPosition].length;
        }

        @Override
        public Object getGroup(int groupPosition){
            return group_title[groupPosition];
        }

        @Override
        public int getGroupCount(){
            return group_title.length;
        }

        @Override
        public long getGroupId(int groupPosition){
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent){
            convertView = getLayoutInflater().inflate(R.layout.mail_group, null);

            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            ImageView iv = (ImageView) convertView.findViewById(R.id.iv);
            TextView ivTitle = (TextView) convertView.findViewById(R.id.iv_title);

            iv.setImageResource(R.drawable.group_right);
            ivTitle.setText(group_title[groupPosition]);

            if (groupPosition == 0){
                icon.setImageResource(R.drawable.constants);
            }else if (groupPosition == 1){
                icon.setImageResource(R.drawable.mail_to);
            }else if (groupPosition == 2){
                icon.setImageResource(R.drawable.mail_box);
            }

            if (group_click[groupPosition] % 2 == 0){
                iv.setImageResource(R.drawable.group_right);
            }else {
                iv.setImageResource(R.drawable.group_down);
            }

            return convertView;
        }

        @Override
        public boolean hasStableIds(){
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition){
            return  true;
        }
    }

    private void insertAddress(String user, String address){
        if (user == null){
            Toast.makeText(MainActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
        }else {
            if (!EmailFormatUtil.emailFormat(address)){
                Toast.makeText(MainActivity.this, "邮箱格式不正确", Toast.LENGTH_SHORT).show();
            }else {
                Uri uri = Uri.parse("content://com.zjw.mailconstantprovider");
                ContentValues values = new ContentValues();

                values.put("mailfrom", MyApplication.info.getUsername());
                values.put("name", user);
                values.put("address", address);

                getContentResolver().insert(uri, values);

                Toast.makeText(MainActivity.this, "添加数据成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        if (keyCode == KeyEvent.KEYCODE_BACK){
            if ((System.currentTimeMillis() - mExitTime) < 2000){
                android.os.Process.killProcess(android.os.Process.myPid());
            }else {
                Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
