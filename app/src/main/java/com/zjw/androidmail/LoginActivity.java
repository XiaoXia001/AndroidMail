package com.zjw.androidmail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.zjw.androidmail.app.MyApplication;
import com.zjw.androidmail.utils.EmailFormatUtil;
import com.zjw.androidmail.utils.HttpUtil;

public class LoginActivity extends AppCompatActivity implements TextWatcher, OnClickListener {

    private EditText emailAddress;

    private EditText password;

    private Button clearAddress;

    private Button seePassword;

    public Boolean seePwd = true;

    private Button emailLogin;

    private CheckBox cbRemember;

    private CheckBox cbAutoLogin;

    private ProgressDialog dialog;

    private SharedPreferences sharedPreferences;

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg){
            if (MyApplication.session == null){
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "账号或密码不正确", Toast.LENGTH_SHORT).show();
            }else {
                dialog.dismiss();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(LoginActivity.this, "登入成功", Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_login);
        sharedPreferences = getSharedPreferences("config", Context.MODE_APPEND);

        emailAddress = (EditText) findViewById(R.id.email_address);
        password = (EditText) findViewById(R.id.password);
        clearAddress = (Button) findViewById(R.id.clear_address);
        seePassword = (Button) findViewById(R.id.see_password);
        emailLogin = (Button) findViewById(R.id.login_button);
        cbRemember = (CheckBox) findViewById(R.id.remember_password);
        cbAutoLogin = (CheckBox) findViewById(R.id.auto_login);


        clearAddress.setOnClickListener(this);
        emailLogin.setOnClickListener(this);
        cbRemember.setOnClickListener(this);
        cbAutoLogin.setOnClickListener(this);
        seePassword.setOnClickListener(this);

        isRememberPwd();



        emailAddress.addTextChangedListener(this);

    }

    @Override
    public void onClick(View view){

        switch (view.getId()){

            case R.id.clear_address:
                emailAddress.setText("");
                break;
            case R.id.remember_password:
                rememberPwd();
                break;
            case R.id.auto_login:
                //cbAutoLogin.setChecked(sharedPreferences.getBoolean("isAutoLogin", false));
                if (cbAutoLogin.isChecked()){
                    cbAutoLogin.setChecked(true);
                    sharedPreferences.edit().putBoolean("isAutoLogin", true).commit();
                }else {
                    cbAutoLogin.setChecked(false);
                    sharedPreferences.edit().putBoolean("isAutoLogin", false).commit();
                }
                break;
            case R.id.login_button:
                loginEmail();
                break;
            case R.id.see_password:
                if (seePwd){
                    seePwd = false;
                    seePassword.setBackgroundResource(R.drawable.login_input_invisible);
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else {
                    seePwd = true;
                    seePassword.setBackgroundResource(R.drawable.login_input_visible);
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count){
        if (!TextUtils.isEmpty(s)){
            clearAddress.setVisibility(View.VISIBLE);
        }else {
            clearAddress.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after){

    }

    @Override
    public void afterTextChanged(Editable s){

    }

    public void rememberPwd(){
        Boolean isRbPwd = sharedPreferences.getBoolean("isRbPwd", false);
        if (isRbPwd){
            sharedPreferences.edit().putBoolean("isRbPwd", false).commit();
            cbRemember.setChecked(false);
        }else {
            sharedPreferences.edit().putBoolean("isRbPwd", true).commit();
            sharedPreferences.edit().putString("emailAddress", emailAddress.getText().toString().trim()).commit();
            sharedPreferences.edit().putString("password", password.getText().toString().trim()).commit();
            cbRemember.setChecked(true);
        }
    }

    private void loginEmail(){
        String address = emailAddress.getText().toString().trim();
        String pwd = password.getText().toString().trim();
        if (TextUtils.isEmpty(address)){
            Toast.makeText(LoginActivity.this, "地址不能为空", Toast.LENGTH_SHORT).show();;
            return;
        }else {
            if (TextUtils.isEmpty(pwd)){
                Toast.makeText(LoginActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();;
                return;
            }
        }

        if (!EmailFormatUtil.emailFormat(address)){
            Toast.makeText(LoginActivity.this, "邮箱格式不正确", Toast.LENGTH_SHORT).show();
        }else {
            String host = "smtp." + address.substring(address.lastIndexOf("@") + 1);
            MyApplication.info.setMailServerHost(host);
            MyApplication.info.setMailServerPort("25");
            MyApplication.info.setUsername(address);
            MyApplication.info.setPassword(pwd);
            MyApplication.info.setValidate(true);


            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setMessage("正在登入，请稍候");
            dialog.show();

            new Thread() {

                @Override
                public void run() {
                    HttpUtil util = new HttpUtil();
                    MyApplication.session = util.login();
                    Message message = handler.obtainMessage();
                    message.sendToTarget();
                }
            }.start();
        }
    }

    public void isRememberPwd(){
        Boolean isRbPwd = sharedPreferences.getBoolean("isRbPwd", false);
        if (isRbPwd){
            String addr = sharedPreferences.getString("emailAddress", "");
            String pwd = sharedPreferences.getString("password", "");
            emailAddress.setText(addr);
            password.setText(pwd);
            cbRemember.setChecked(true);
            cbAutoLogin.setChecked(sharedPreferences.getBoolean("isAutoLogin", false));
            //Boolean isAutoLogin = sharedPreferences.getBoolean("isAutoLogin", false);
            if (cbAutoLogin.isChecked()){
                emailLogin.performClick();
                //cbAutoLogin.setChecked(true);
                //Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                //LoginActivity.this.startActivity(intent);
            }
        }
    }
}
