package com.example.asuspc.nomalUserActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.example.asuspc.ManageActivity.BaseActivity;
import com.example.asuspc.ManageActivity.LoginActivity;
import com.example.asuspc.baidumapapp.FindMyAndOther;
import com.example.asuspc.baidumapapp.R;
import com.example.asuspc.businessOwnerActivity.ManagerActivity;
import com.example.asuspc.entity.Business;
import com.example.asuspc.entity.Dining;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class SeeDetailActivity extends Activity implements BaseActivity ,SeeAdapter.OnSeeSlideClickListener{

    private TextView see_business_name;
    private ImageView see_business_photo;
    private TextView see_business_phone;
    private Button see_business_call;
    private TextView see_business_address;
    private ImageView imageSmall;
    private ListView see_dininglist;
    private Button see_return;
    Business see_business;
    String username;
    String business_name;
    String business_user_name;
    ArrayList<Dining> diningList;
    SeeAdapter seeAdapter;

    private Handler handler;
    private Thread databaseThread;
    AlertDialog waiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_detail);
        init();
        findData();
        addEvent();
    }

    @Override
    public void init() {
        see_business_name= (TextView) findViewById(R.id.see_business_name);
        see_business_photo= (ImageView) findViewById(R.id.see_business_photo);
        see_business_phone= (TextView) findViewById(R.id.see_business_phone);
        see_business_call= (Button) findViewById(R.id.see_business_call);
        see_business_address= (TextView) findViewById(R.id.see_business_address);
        imageSmall= (ImageView) findViewById(R.id.imageSmall);
        see_dininglist= (ListView) findViewById(R.id.see_dininglist);
        see_return= (Button) findViewById(R.id.see_return);
        Intent intent = this.getIntent();
        username = intent.getStringExtra("username");
        business_name = intent.getStringExtra("business_name");
        see_business=new Business();
    }

    @Override
    public void addEvent() {
        see_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("username",username);
                intent.setClass(SeeDetailActivity.this, FindMyAndOther.class);
                SeeDetailActivity.this.startActivity(intent);
            }
        });
        see_business_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+see_business_phone.getText().toString()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
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
                findBusiness(con);
            }
        });
        handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 0x123) {
                    if (msg.obj.equals("yes")) {
                        setData();
                        waiting.dismiss();
                    } else {
                        waiting.dismiss();
                        waiting=new AlertDialog.Builder(SeeDetailActivity.this).setTitle("系统提示")
                                .setMessage("未发现所点击店铺信息，可能是该商家未加入此系统。" +
                                        "")//设置显示的内容
                                .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setClass(SeeDetailActivity.this, FindMyAndOther.class);
                                        SeeDetailActivity.this.startActivity(intent);

                                    }
                                })
                                .show();
                    }
                }
                if(msg.what==0x234){
                    seeAdapter= new SeeAdapter(SeeDetailActivity.this,diningList,SeeDetailActivity.this);
                    see_dininglist.setAdapter(seeAdapter);
                }
            }
        };
        databaseThread.start();
        waiting= new AlertDialog.Builder(SeeDetailActivity.this).setTitle("系统提示")//设置对话框标题
                .setMessage("请等候，正在获取")
                .show();
    }
    public void setData(){
        see_business_name.setText(see_business.getBusiness_name());
        see_business_phone.setText(see_business.getBusiness_phone());
        see_business_address.setText(see_business.getBusiness_address());
        if(see_business.getBusiness_picture()!=null){
            see_business_photo.setImageBitmap(BitmapFactory.decodeByteArray(
                    see_business.getBusiness_picture(), 0, (see_business.getBusiness_picture()).length));
        }
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
                findDining(con);
            }
        });
        databaseThread.start();

    }

    public void findBusiness(Connection con){
        String sql = "select * from business where business_name=?";
        PreparedStatement pstmt =null;
        int temp=0;
        try {
            pstmt= con.prepareStatement(sql);
            pstmt.setString(1, business_name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                see_business.setBusiness_id(rs.getString("business_id"));
                see_business.setBusiness_name(rs.getString("business_name"));
                see_business.setBusiness_address(rs.getString("business_address"));
                see_business.setBusiness_phone(rs.getString("business_phone"));
                see_business.setBusiness_owner(rs.getString("business_owner"));
                business_user_name=rs.getString("business_owner");
                see_business.setBusiness_picture(rs.getBytes("business_picture"));
                see_business.setBusiness_jingdu(rs.getString("business_jingdu"));
                see_business.setBusiness_weidu(rs.getString("business_weidu"));
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
    public void findDining(Connection con){
        String sql = "select * from dining where comeform=?";
        PreparedStatement pstmt =null;
        int temp=0;
        try {
            pstmt= con.prepareStatement(sql);
            pstmt.setString(1, business_user_name);
            ResultSet rs = pstmt.executeQuery();
            diningList=new ArrayList<Dining>();
            while(rs.next()){
                Dining dining=new Dining();
                dining.setDinging_name(rs.getString("dining_name"));
                dining.setDining_comeform(rs.getString("comeform"));
                dining.setDining_id(rs.getString("dining_id"));
                dining.setDining_price(rs.getInt("dining_price"));
                dining.setDining_picture(rs.getBytes("dining_picture"));
                diningList.add(dining);
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
        message.what = 0x234;
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
    public void onAddClick(int position, View item) {
        final String name=diningList.get(position).getDining_comeform();
        Log.e("haha", "onAddClick: "+name );
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
                addBusinessList(con,name);
            }
        });
        databaseThread.start();
    }
    public void addBusinessList(Connection con,String name){
        String sql="insert into businesslist values(?,?,0)";
        PreparedStatement pstmt = null;
        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,username);
            pstmt.setString(2,name);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

    }
    // 捕获返回键的方法2
    @Override
    public void onBackPressed() {
        databaseThread.interrupt();
        SeeDetailActivity.this.finish();
        SeeDetailActivity.super.onBackPressed();
    }
}
