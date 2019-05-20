package com.example.asuspc.businessOwnerActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.service.LocationService;
import com.example.asuspc.ManageActivity.BaseActivity;
import com.example.asuspc.ManageActivity.LoginActivity;
import com.example.asuspc.baidumapapp.LocationApplication;
import com.example.asuspc.baidumapapp.R;
import com.example.asuspc.entity.Business;
import com.example.asuspc.entity.Dining;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;


public class ManagerActivity extends Activity implements BaseActivity ,MyAdapter.OnSlideClickListener{
    Business business;
    ArrayList<Dining> diningList;
    String username;
    private Handler handler;
    private Thread databaseThread;
    AlertDialog waiting;
    private EditText bm_business_name;
    EditText bm_business_phone;
    Button bm_adddidning;
    ImageView mainImage;
    ListView diningView;
    MyAdapter myAdapter;
    Button bm_update;
    EditText bm_business_address;
    Button bm_changeAddress;
    ImageView imageView7;
    Button bm_see;
    Button bm_seeuser;
    private LocationService locationService;

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
                findBusiness(con, username);
            }
        });
        handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 0x123) {
                    if (msg.obj.equals("yes")) {
                        setData();
                        databaseThread.start();
                        waiting.dismiss();
                    } else {
                        waiting.dismiss();
                        waiting=new AlertDialog.Builder(ManagerActivity.this).setTitle("系统提示")
                                .setMessage("未发现您的账户信息，前往添加页面进行添加。" +
                                        "或者选择返回登录页面重新登录。")//设置显示的内容
                                .setPositiveButton("前往", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.putExtra("username", username);
                                        intent.setClass(ManagerActivity.this, AddNewBusinessActivity.class);
                                        ManagerActivity.this.startActivity(intent);

                                    }
                                }).setNegativeButton("返回", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setClass(ManagerActivity.this, LoginActivity.class);
                                        ManagerActivity.this.startActivity(intent);
                                    }
                                })
                                .show();
                    }
                }
                if(msg.what==0x234){
                    myAdapter= new MyAdapter(ManagerActivity.this,diningList,ManagerActivity.this);
                    diningView.setAdapter(myAdapter);
                    updateBusiness();
                }
            }
        };
        databaseThread.start();
        waiting= new AlertDialog.Builder(ManagerActivity.this).setTitle("系统提示")//设置对话框标题
                .setMessage("请等候，正在获取")
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        init();

    }

    @Override
    protected void onStart() {
        super.onStart();
        setBaiduDW();
        findData();
        addEvent();

    }

    @Override
    public void init() {
        Intent intent = this.getIntent();
        username = intent.getStringExtra("username");
        business=new Business();
        bm_adddidning=(Button) findViewById(R.id.bm_adddining);
        bm_business_name=(EditText)findViewById(R.id.bm_business_name);
        bm_business_phone=(EditText)findViewById(R.id.bm_business_phone);
        mainImage=(ImageView)findViewById(R.id.bm_business_photo);
        diningView=(ListView)findViewById(R.id.diningList);
        bm_update=(Button)findViewById(R.id.bm_update);
        bm_business_address=(EditText)findViewById(R.id.bm_business_address);
        bm_changeAddress=(Button)findViewById(R.id.bm_changeAddress);
        imageView7=(ImageView)findViewById(R.id.imageView7);
        imageView7.setImageResource(R.drawable.huaji);
        bm_see= (Button) findViewById(R.id.bm_see);
        bm_seeuser=(Button) findViewById(R.id.bm_seeuser);
    }
    public void setData(){
        bm_business_name.setText(business.getBusiness_name());
        bm_business_phone.setText(business.getBusiness_phone());
        bm_business_address.setText(business.getBusiness_address());
        if(business.getBusiness_picture()!=null){
            mainImage.setImageBitmap(BitmapFactory.decodeByteArray(
                    business.getBusiness_picture(), 0, (business.getBusiness_picture()).length));
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

    }

    @Override
    public void addEvent() {
        bm_adddidning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("username", username);
                intent.putExtra("type","add");
                intent.setClass(ManagerActivity.this, AddNewDiningActivity.class);
                ManagerActivity.this.startActivity(intent);
            }
        });
        bm_see.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("username", username);
                intent.putExtra("jingdu", business.getBusiness_jingdu());
                intent.putExtra("weidu",business.getBusiness_weidu());
                intent.setClass(ManagerActivity.this, BusinessSeeMyActivity.class);
                ManagerActivity.this.startActivity(intent);
            }
        });
        bm_seeuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("username", username);
                intent.setClass(ManagerActivity.this, SeeUserActivity.class);
                ManagerActivity.this.startActivity(intent);
            }
        });
    }
    public void findBusiness(Connection con,String username){
        String sql = "select * from business where business_owner=?";
        PreparedStatement pstmt =null;
        int temp=0;
        try {
            pstmt= con.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                business.setBusiness_id(rs.getString("business_id"));
                business.setBusiness_name(rs.getString("business_name"));
                business.setBusiness_address(rs.getString("business_address"));
                business.setBusiness_phone(rs.getString("business_phone"));
                business.setBusiness_owner(rs.getString("business_owner"));
                business.setBusiness_picture(rs.getBytes("business_picture"));
                business.setBusiness_jingdu(rs.getString("business_jingdu"));
                business.setBusiness_weidu(rs.getString("business_weidu"));
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
            pstmt.setString(1, username);
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
    public void deleteDining(Connection con,Dining tangshi){
        String sql="delete from dining where dining_name=? and comeform=?";
        PreparedStatement pstmt = null;
        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,tangshi.getDinging_name());
            pstmt.setString(2,username);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

    }
    public void updateBusiness(Connection con) throws java.sql.SQLException {
        int temp = 0;
        String sql;
        PreparedStatement pstmt =null;
        sql="UPDATE business SET business_name=?,business_address=?,business_phone=?,business_jingdu=?,business_weidu=? WHERE business_id = ?";
        pstmt= con.prepareStatement(sql);
        pstmt.setString(1, business.getBusiness_name());
        pstmt.setString(2, business.getBusiness_address());
        pstmt.setString(3, business.getBusiness_phone());
        pstmt.setString(4, business.getBusiness_jingdu());
        pstmt.setString(5, business.getBusiness_weidu());
        pstmt.setString(6, business.getBusiness_id());
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
    @Override
    public void onUpdateClick(int position, View item) {
        Intent intent = new Intent();
        intent.putExtra("username", username);
        intent.putExtra("id",diningList.get(position).getDining_id());
        intent.putExtra("type","update");
        intent.setClass(ManagerActivity.this, AddNewDiningActivity.class);
        ManagerActivity.this.startActivity(intent);
    }

    @Override
    public void onDeleteClick(int position, View item) {
        final Dining tangshi=diningList.get(position);
        diningList.remove(position);
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
                deleteDining(con,tangshi);
            }
        });
        databaseThread.start();
        myAdapter.notifyDataSetChanged();
    }
    public void updateBusiness(){
        bm_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                business.setBusiness_name(bm_business_name.getText().toString());
                business.setBusiness_address(bm_business_address.getText().toString());
                business.setBusiness_phone(bm_business_phone.getText().toString());
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
                            updateBusiness(con);
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
                                Toast.makeText(ManagerActivity.this,"成功！！！",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                };
                databaseThread.start();
                waiting= new AlertDialog.Builder(ManagerActivity.this).setTitle("系统提示")//设置对话框标题
                        .setMessage("请等候，正在上传")
                        .show();
            }
        });
    }
    /**
     * 显示请求字符串
     *
     * @param str
     */
    public void logMsg(String str) {
        final String s = str;
        try {
            if (bm_business_address != null){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        bm_business_address.post(new Runnable() {
                            @Override
                            public void run() {
                                bm_business_address.setText(s);
                            }
                        });

                    }
                }).start();
            }
            //LocationResult.setText(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBaiduDW(){
        //设置EditText的显示方式为多行文本输入
        bm_business_address.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        //文本显示的位置在EditText的最上方
        bm_business_address.setGravity(Gravity.TOP);
        //改变默认的单行模式
        bm_business_address.setSingleLine(false);

        locationService = ((LocationApplication) getApplication()).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //注册监听
        int type = getIntent().getIntExtra("from", 0);
        if (type == 0) {
            locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        } else if (type == 1) {
            locationService.setLocationOption(locationService.getOption());
        }
        bm_changeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bm_changeAddress.getText().toString().equals(getString(R.string.startlocation))) {
                    locationService.start();// 定位SDK
                    // start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
                    bm_changeAddress.setText(getString(R.string.stoplocation));
                } else {
                    locationService.stop();
                    bm_changeAddress.setText(getString(R.string.startlocation));
                }
            }
        });
    }
    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                StringBuffer sb = new StringBuffer(256);
/*                sb.append("\nlatitude : ");// 纬度
                sb.append(location.getLatitude());

                sb.append("\nlontitude : ");// 经度
                sb.append(location.getLongitude());*/
                //城市
               /* sb.append(location.getCity());*/
                //区
                /*sb.append(location.getDistrict());*/

                //纬度
                business.setBusiness_weidu(location.getLatitude()+"");
                //经度
                business.setBusiness_jingdu(location.getLongitude()+"");

                // 街道
                sb.append(location.getStreet());
                // 地址信息
                sb.append(location.getAddrStr());
                sb.append(location.getLocationDescribe());// 位置语义化信息
                if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                   if (location.hasAltitude()) {
                    sb.append("网络定位成功");
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError)
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
                logMsg(sb.toString());
            }
        }

    };

    public void onBackPressed() {
        this.finish();
        databaseThread.interrupt();
        Intent intent = new Intent();
        intent.putExtra("username",this.getIntent().getStringExtra("username"));
        intent.setClass(ManagerActivity.this, LoginActivity.class);
        ManagerActivity.this.startActivity(intent);
    }
}
