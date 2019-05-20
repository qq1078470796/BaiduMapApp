package com.example.asuspc.ManageActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.asuspc.baidumapapp.R;
import com.example.asuspc.entity.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class RegisterActivity extends Activity implements BaseActivity{
    User registor;
    EditText username;
    EditText password;
    EditText password_again;
    EditText phoneNum;
    RadioButton nomal_user;
    RadioButton master;
    Button submit;
    Button clear;
    private Handler handler;
    private Thread databaseThread;
    public void init(){
        registor=new User();
        username=(EditText)findViewById(R.id.r_username);
        password=(EditText)findViewById(R.id.r_password);
        password_again=(EditText)findViewById(R.id.r_password_again);
        nomal_user=(RadioButton) findViewById(R.id.radioButton2);
        master=(RadioButton) findViewById(R.id.radioButton);
        submit=(Button) findViewById(R.id.r_submit);
        clear=(Button) findViewById(R.id.r_clear);
        phoneNum=(EditText) findViewById(R.id.r_phone);
    }
    public void addEvent(){
        password_again.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String pass=password.getText().toString();
                    String pass_a=password_again.getText().toString();
                    if(!pass.equals(pass_a)){
                        Toast.makeText(RegisterActivity.this,"两次密码不一致，请重新输入",Toast.LENGTH_SHORT).show();
                        password.setText("");
                        password_again.setText("");
                    }
                }
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username.setText("");
                password.setText("");
                password_again.setText("");
                phoneNum.setText("");
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registor.setUsername(username.getText().toString());
                registor.setPassword(password.getText().toString());
                registor.setPhone(phoneNum.getText().toString());
                if(nomal_user.isChecked())
                registor.setType(1);
                else{
                    registor.setType(0);
                }
                databaseThread.start();
            }
        });
        databaseThread =new Thread(new Runnable() {
            @Override
            public void run() {
                Connection con = null;
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    con= DriverManager.getConnection("jdbc:mysql://120.78.185.195/baidumap","root","root");
                } catch (ClassNotFoundException e) {
                    System.out.println("加载驱动程序出错");
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                registerAction(con,registor);

            }
        });
        //接收从子线程发回来的结果进行处理
        handler = new Handler(){
            public void handleMessage(Message msg) {
                if(msg.what == 0x123){
                    if(msg.obj.equals("yes")){
                        new AlertDialog.Builder(RegisterActivity.this).setTitle("系统提示")//设置对话框标题

                                .setMessage("注册成功")//设置显示的内容
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                       Intent intent = new Intent();
                                        intent.setClass(RegisterActivity.this, LoginActivity.class);
                                        RegisterActivity.this.startActivity(intent);


                                    }
                                }).show();
                    }
                    else{
                        new AlertDialog.Builder(RegisterActivity.this).setTitle("系统提示")//设置对话框标题

                                .setMessage("账号已存在，请重新注册")//设置显示的内容
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        username.setText("");
                                        password.setText("");
                                        password_again.setText("");
                                        phoneNum.setText("");
                                    }
                                }).show();
                    }
                }
            }
        };
    }
    public void registerAction(Connection con,User user){
        String sql = "insert into user values(?,?,?,?)";
        PreparedStatement pstmt =null;
        int temp=0;
        try {
            pstmt= con.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setInt(3, user.getType());
            pstmt.setString(4, user.getPhone());
            temp= pstmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        try {
            pstmt.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        Message message = new Message();
        message.what = 0x123;
        if(temp==1){
            message.obj = "yes";
        }
        else{
            message.obj = "no";
        }
        //消息从子线程发回主线程
        handler.sendMessage(message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
        addEvent();
    }
}
