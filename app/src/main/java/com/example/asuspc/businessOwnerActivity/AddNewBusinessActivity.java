package com.example.asuspc.businessOwnerActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.service.LocationService;
import com.example.asuspc.ManageActivity.BaseActivity;
import com.example.asuspc.ManageActivity.LoginActivity;
import com.example.asuspc.baidumapapp.LocationApplication;
import com.example.asuspc.baidumapapp.R;
import com.example.asuspc.entity.Business;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.UUID;

public class AddNewBusinessActivity extends Activity implements BaseActivity{

    private EditText b_username;
    private EditText business_name;
    private EditText business_address;
    private EditText business_phone;
    private Button b_submit;
    private Button set_address;
    Business business;
    private Handler handler;
    private Thread databaseThread;
    private Button add_picture;
    private LocationService locationService;

    Uri uritempFile;
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_business);
        init();
        setBaiduDW();
        addEvent();

    }

    @Override
    public void init() {
        b_username=(EditText)findViewById(R.id.ba_username);
        business_address=(EditText)findViewById(R.id.ba_business_address);
        business_phone=(EditText)findViewById(R.id.ba_business_phone);
        business_name=(EditText)findViewById(R.id.ba_business_name);
        b_submit=(Button)findViewById(R.id.ba_submit);
        set_address=(Button)findViewById(R.id.set_address);
        Intent intent = this.getIntent();
        b_username.setText(intent.getStringExtra("username"));
        b_username.setFocusable(false);
        b_username.setFocusableInTouchMode(false);
        business=new Business();
        business.setBusiness_jingdu("0");
        business.setBusiness_weidu("0");
        add_picture=(Button)findViewById(R.id.ba_addpicture);
    }
    public void addNew(Connection con,Business business){
        String sql = "insert into business values(?,?,?,?,?,?,?,?,?)";
        PreparedStatement pstmt =null;
        int temp=0;
        try {
            pstmt= con.prepareStatement(sql);
            pstmt.setString(1, business.getBusiness_id());
            pstmt.setString(2, business.getBusiness_name());
            pstmt.setString(3, business.getBusiness_address());
            pstmt.setString(4,business.getBusiness_phone());
            pstmt.setInt(5,business.getBusiness_usernum());
            pstmt.setString(6,business.getBusiness_owner());
            pstmt.setString(7,business.getBusiness_jingdu());
            pstmt.setString(8,business.getBusiness_weidu());
            pstmt.setBytes(9,business.getBusiness_picture());
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
    public void addEvent() {
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
                addNew(con,business);
            }
        });
        //接收从子线程发回来的结果进行处理
        handler = new Handler(){
            public void handleMessage(Message msg) {
                if(msg.what == 0x123){
                    if(msg.obj.equals("yes")){
                        new AlertDialog.Builder(AddNewBusinessActivity.this).setTitle("系统提示")//设置对话框标题

                                .setMessage("添加成功，即将返回管理页面")//设置显示的内容
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.putExtra("username", business.getBusiness_owner());
                                        intent.setClass(AddNewBusinessActivity.this, ManagerActivity.class);
                                        AddNewBusinessActivity.this.startActivity(intent);
                                    }
                                }).show();
                    }
                }
            }
        };
        add_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhoto();
            }
        });
        b_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                business.setBusiness_owner(b_username.getText().toString());
                business.setBusiness_phone(business_phone.getText().toString());
                business.setBusiness_address(business_address.getText().toString());
                business.setBusiness_id(UUID.randomUUID().toString().substring(0,10));
                business.setBusiness_name(business_name.getText().toString());
                business.setBusiness_usernum(0);
                databaseThread.start();
            }
        });

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
        intent.putExtra("aspectX", 2);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 600);
        intent.putExtra("outputY", 200);
        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        /*intent.putExtra("return-data", true);*/
        uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
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
               /* Bitmap bitmap = data.getParcelableExtra("data");*/
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uritempFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                business.setBusiness_picture(os.toByteArray());
            }
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 显示请求字符串
     *
     * @param str
     */
    public void logMsg(String str) {
        final String s = str;
        try {
            if (business_address != null){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        business_address.post(new Runnable() {
                            @Override
                            public void run() {
                                business_address.setText(s);
                            }
                        });

                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBaiduDW(){
        //设置EditText的显示方式为多行文本输入
        business_address.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        //文本显示的位置在EditText的最上方
        business_address.setGravity(Gravity.TOP);
        //改变默认的单行模式
        business_address.setSingleLine(false);

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
        set_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (set_address.getText().toString().equals(getString(R.string.startlocation))) {
                    locationService.start();// 定位SDK
                    // start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
                    set_address.setText(getString(R.string.stoplocation));
                } else {
                    locationService.stop();
                    set_address.setText(getString(R.string.startlocation));
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
                // 街道
                business.setBusiness_weidu(location.getLatitude()+"");
                business.setBusiness_jingdu(location.getLongitude()+"");
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
    // 捕获返回键的方法2
    @Override
    public void onBackPressed() {
        this.finish();
        databaseThread.interrupt();
        Intent intent = new Intent();
        intent.putExtra("username",this.getIntent().getStringExtra("username"));
        intent.setClass(AddNewBusinessActivity.this, ManagerActivity.class);
        AddNewBusinessActivity.this.startActivity(intent);
    }
}
