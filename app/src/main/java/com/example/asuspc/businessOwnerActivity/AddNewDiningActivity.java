package com.example.asuspc.businessOwnerActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.example.asuspc.ManageActivity.BaseActivity;
import com.example.asuspc.baidumapapp.R;
import com.example.asuspc.entity.Dining;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.UUID;


public class AddNewDiningActivity extends Activity implements BaseActivity {

    private EditText ad_dining_name;
    private EditText ad_dinning_price;
    private EditText ad_dining_comeform;
    private Button ad_dining_apicture;
    private Button ad_dining_submit;
    String username;
    private Dining dining;
    private Handler handler;
    private Thread databaseThread;
    String type;
    String id;
    Dining updateDining;
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果

    private ImageView iv_image;
    byte[] picture;
    AlertDialog waiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_dining);
        init();
        if(type.equals("add")){
            addEvent();
        }else if(type.equals("update")){
            id=this.getIntent().getStringExtra("id");
            updateDining=new Dining();
            selectEvent();
            updateEvent();
        }

    }

    @Override
    public void init() {
        ad_dining_name=(EditText)findViewById(R.id.ad_dining_name);
        ad_dining_comeform=(EditText)findViewById(R.id.ad_dining_comeform);
        ad_dining_submit=(Button)findViewById(R.id.ad_dining_submit);
        ad_dining_apicture=(Button)findViewById(R.id.ad_dining_apicture);
        ad_dinning_price=(EditText)findViewById(R.id.ad_dinning_price);
        Intent intent = this.getIntent();
        username = intent.getStringExtra("username");
        dining=new Dining();
        ad_dining_comeform.setText(username);
        dining.setDining_comeform(username);
        ad_dining_comeform.setFocusableInTouchMode(false);
        ad_dining_comeform.clearFocus();
        iv_image=(ImageView)findViewById(R.id.testImageView);
        type=intent.getStringExtra("type");
    }

    /**
     * 打开本地文件选择器
     */
    private void pickPhoto() {
        // 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }

    /**
     * 裁切图片
     * @param uri
     */
    private void crop(Uri uri) {
            // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    /**
     * 选择完之后执行的返回结果操作
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            // 从相册返回的数据
            Uri uri;
            if (data != null) {
                // 得到图片的全路径
                uri= data.getData();
                Log.e("haha", "uri = "+ uri);
                crop(uri);
            }
        } else if (requestCode == PHOTO_REQUEST_CUT) {
            // 从剪切图片返回的数据
            if (data != null) {
                Bitmap bitmap = data.getParcelableExtra("data");
                this.iv_image.setImageBitmap(bitmap);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                picture=os.toByteArray();

                dining.setDining_id(UUID.randomUUID().toString());
                dining.setDinging_name(ad_dining_name.getText().toString());
                dining.setDining_price(Double.parseDouble(ad_dinning_price.getText().toString()));
                dining.setDining_picture(picture);
            }
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void addEvent() {
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
                addNewDining(con);
            }
        });
        handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 0x123) {
                    if (msg.obj.equals("yes")) {
                        waiting.dismiss();
                        Intent intent = new Intent();
                        intent.putExtra("username", username);
                        intent.setClass(AddNewDiningActivity.this, ManagerActivity.class);
                        AddNewDiningActivity.this.startActivity(intent);
                    }
                }
            }
        };
        ad_dining_apicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhoto();
            }
        });
        ad_dining_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseThread.start();
                waiting= new AlertDialog.Builder(AddNewDiningActivity.this).setTitle("系统提示")//设置对话框标题
                        .setMessage("请等候，正在上传")
                        .show();
            }
        });
    }
    public void addNewDining(Connection con){
        String sql = "insert into dining values(?,?,?,?,?)";
        PreparedStatement pstmt =null;
        int temp=0;
        try {
            pstmt= con.prepareStatement(sql);
            pstmt.setString(1, dining.getDining_id());
            pstmt.setString(2, dining.getDinging_name());
            pstmt.setDouble(3, dining.getDining_price());
            pstmt.setString(4, dining.getDining_comeform());
            pstmt.setBytes(5, dining.getDining_picture());
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
    public void selectDining(Connection con) throws java.sql.SQLException {
        int temp=0;
        String sql = "select * from dining where comeform=? and dining_id=?";
        PreparedStatement pstmt =null;
        dining=new Dining();
        pstmt= con.prepareStatement(sql);
        pstmt.setString(1, username);
        pstmt.setString(2,id);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()){
            dining.setDinging_name(rs.getString("dining_name"));
            dining.setDining_price(rs.getDouble("dining_price"));
            dining.setDining_picture(rs.getBytes("dining_picture"));
            dining.setDining_id(rs.getString("dining_id"));
            temp=1;
        }
        try {
            pstmt.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        con.close();
        Message message = new Message();
        message.what = 0x345;
        if(temp==1){
            message.obj = "yes";
        }
        else{
            message.obj = "no";
        }
        //消息从子线程发回主线程
        handler.sendMessage(message);
    }
    public void updateDining(Connection con) throws java.sql.SQLException {
        int temp = 0;
        String sql;
        PreparedStatement pstmt =null;
        sql="UPDATE dining SET dining_name=?,dining_price=?,dining_picture=? WHERE dining_id = ?";
        pstmt= con.prepareStatement(sql);
        pstmt.setString(1, updateDining.getDinging_name());
        pstmt.setDouble(2,updateDining.getDining_price());
        pstmt.setBytes(3,updateDining.getDining_picture());
        pstmt.setString(4,id);
        Log.e("ads", "updateDining: "+id);
        temp = pstmt.executeUpdate();
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
    public void updateEvent(){
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
                try {
                    updateDining(con);
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        ad_dining_apicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhoto();
            }
        });
        ad_dining_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateDining.setDinging_name(ad_dining_name.getText().toString());
                updateDining.setDining_price(Double.parseDouble(ad_dinning_price.getText().toString()));
                updateDining.setDining_id(id);
                updateDining.setDining_comeform(username);
                updateDining.setDining_picture(dining.getDining_picture());

                databaseThread.start();
                waiting= new AlertDialog.Builder(AddNewDiningActivity.this).setTitle("系统提示")//设置对话框标题
                        .setMessage("请等候，正在上传")
                        .show();
            }
        });
    }
    public void selectEvent(){
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
                try {
                    selectDining(con);
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 0x234) {
                    if (msg.obj.equals("yes")) {
                        waiting.dismiss();
                        Intent intent = new Intent();
                        intent.putExtra("username", username);
                        intent.setClass(AddNewDiningActivity.this, ManagerActivity.class);
                        AddNewDiningActivity.this.startActivity(intent);
                    }
                }
                else if (msg.what == 0x345) {
                    if (msg.obj.equals("yes")) {

                        ad_dining_name.setText(dining.getDinging_name());
                        ad_dinning_price.setText(dining.getDining_price()+"");
                        iv_image.setImageBitmap(BitmapFactory.decodeByteArray(
                                dining.getDining_picture(), 0, (dining.getDining_picture()).length));
                    }
                }
            }
        };
        databaseThread.start();
    }
}
