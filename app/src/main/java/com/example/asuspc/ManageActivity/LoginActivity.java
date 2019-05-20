package com.example.asuspc.ManageActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.asuspc.baidumapapp.FindMyAndOther;
import com.example.asuspc.baidumapapp.R;
import com.example.asuspc.businessOwnerActivity.ManagerActivity;
import com.example.asuspc.entity.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginActivity extends Activity implements BaseActivity {
    User loginer;
    User input;
    private Button l_submit;
    private EditText l_username;
    private EditText l_password;
    private Handler handler;
    private Thread databaseThread;
    public void init(){
        loginer=new User();
        input=new User();
        l_username=(EditText) findViewById(R.id.l_username);
        l_password=(EditText) findViewById(R.id.l_password);
        l_submit=(Button)findViewById(R.id.l_submit);
    }
    public void addEvent(){
        l_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input.setUsername(l_username.getText().toString());
                input.setPassword(l_password.getText().toString());
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
                loginAction(con,input);
            }
        });
        //接收从子线程发回来的结果进行处理
        handler = new Handler(){
            public void handleMessage(Message msg) {
                if(msg.what == 0x123){
                    if(msg.obj.equals("yes")){
                        new AlertDialog.Builder(LoginActivity.this).setTitle("系统提示")//设置对话框标题

                                .setMessage("登录成功")//设置显示的内容
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.putExtra("username", loginer.getUsername());
                                        if(loginer.getType()==0){
                                            intent.setClass(LoginActivity.this, ManagerActivity.class);
                                        }
                                        else{
                                            intent.setClass(LoginActivity.this, FindMyAndOther.class);
                                        }
                                        LoginActivity.this.startActivity(intent);
                                    }
                                }).show();
                    }
                    else{
                        new AlertDialog.Builder(LoginActivity.this).setTitle("系统提示")//设置对话框标题

                                .setMessage("用户名或密码错误")//设置显示的内容
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        l_username.setText("");
                                        l_password.setText("");
                                    }
                                }).show();
                    }
                }
            }
        };
    }
    public void loginAction(Connection con,User user){
        String sql = "select * from user where username=? and password=?";
        PreparedStatement pstmt =null;
        int temp=0;
        try {
            pstmt= con.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                loginer.setUsername(rs.getString("username"));
                loginer.setPassword(rs.getString("password"));
                loginer.setType(rs.getInt("type"));
                loginer.setPhone(rs.getString("phone"));
                temp=1;
            }

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
        setContentView(R.layout.activity_login);
        init();
        addEvent();
    }

    // 捕获返回键的方法2
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(LoginActivity.this).setTitle("系统提示")//设置对话框标题

                .setMessage("确定要注销并退出么")//设置显示的内容
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseThread.interrupt();
                        LoginActivity.this.finish();
                        /**
                         * 彻底退出app
                         */
                        int currentVersion = android.os.Build.VERSION.SDK_INT;
                        if (currentVersion > android.os.Build.VERSION_CODES.ECLAIR_MR1) {
                            Intent startMain = new Intent(Intent.ACTION_MAIN);
                            startMain.addCategory(Intent.CATEGORY_HOME);
                            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(startMain);
                            System.exit(0);
                        } else {// android2.1
                            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                            am.restartPackage(getPackageName());
                        }
                        LoginActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();

    }
}
