package com.example.asuspc.businessOwnerActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.SQLException;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.example.asuspc.ManageActivity.BaseActivity;
import com.example.asuspc.baidumapapp.R;
import com.example.asuspc.entity.BusinessList;
import com.example.asuspc.entity.Dining;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class SeeUserActivity extends Activity implements BaseActivity , SeeUserAdapter.OnSlideClickListener{
    ListView see_user_list;
    String username;
    private Handler handler;
    private Thread databaseThread;
    AlertDialog waiting;
    SeeUserAdapter seeUserAdapter;
    ArrayList<BusinessList> bl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_user);
        init();
    }

    @Override
    public void init() {
        Intent intent = this.getIntent();
        username = intent.getStringExtra("username");
        Log.e("hhh", "init: "+username );
        see_user_list=(ListView)findViewById(R.id.see_user_list);
        findData();
    }
    public void findData() {
        databaseThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Connection con = null;
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    con = DriverManager.getConnection("jdbc:mysql://120.78.185.195/baidumap", "root", "root");
                } catch (ClassNotFoundException e) {
                    System.out.println("加载驱动程序出错");
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                findBusinessList(con, username);
            }
        });
        handler = new Handler() {
            public void handleMessage(Message msg) {
                if(msg.what==0x234){
                    seeUserAdapter = new SeeUserAdapter(SeeUserActivity.this,bl,SeeUserActivity.this);
                    see_user_list.setAdapter(seeUserAdapter);
                    waiting.dismiss();
                }
            }
        };
        databaseThread.start();
        waiting= new AlertDialog.Builder(SeeUserActivity.this).setTitle("系统提示")//设置对话框标题
                .setMessage("请等候，正在获取")
                .show();
    }
    public void findBusinessList(Connection con,String username){
        String sql = "select * from businesslist where businessname=?";
        PreparedStatement pstmt =null;
        int temp=0;
        try {
            pstmt= con.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            bl=new ArrayList<BusinessList>();
            while(rs.next()){
                BusinessList t=new BusinessList();
                t.setBusinessName(rs.getString("businessname"));
                t.setUserName(rs.getString("username"));
                bl.add(t);
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
        message.what = 0x234;
        //消息从子线程发回主线程
        handler.sendMessage(message);
    }


    @Override
    public void addEvent() {

    }

    @Override
    public void onDeleteClick(int position, View item) {
        final String name=bl.get(position).getUserName();
        bl.remove(position);
        databaseThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Connection con = null;
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    con = DriverManager.getConnection("jdbc:mysql://120.78.185.195/baidumap", "root", "root");
                } catch (ClassNotFoundException e) {
                    System.out.println("加载驱动程序出错");
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                deleteBusinessList(con,name);
            }
        });
        databaseThread.start();
        seeUserAdapter.notifyDataSetChanged();
    }
    public void deleteBusinessList(Connection con,String name){
        String sql="delete from businesslist where username=? and businessname=?";
        PreparedStatement pstmt = null;
        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,name);
            pstmt.setString(2,username);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

    }
}
