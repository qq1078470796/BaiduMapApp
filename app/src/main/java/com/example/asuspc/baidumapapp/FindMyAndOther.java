package com.example.asuspc.baidumapapp;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.example.asuspc.ManageActivity.LoginActivity;
import com.example.asuspc.businessOwnerActivity.AddNewBusinessActivity;
import com.example.asuspc.businessOwnerActivity.ManagerActivity;
import com.example.asuspc.nomalUserActivity.SeeDetailActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FindMyAndOther extends Activity {

    private MapView mMapView;// 地图视图
    private BaiduMap mBaiduMap;// 地图控制器 setMapStatus(mMapStatusUpdate);
    private MapStatus mMapStatus;// 地图当前状态
    private MapStatusUpdate mMapStatusUpdate;// 地图将要变化成的状态
    private Button btn_location;// 定位button
    private Button btn_search;// 搜索button

    public LocationClient mLocationClient = null;// 定位的核心类:LocationClient
    public BDLocationListener myLocationListener = new MyLocationListener();// 定位的回调接口
    private LatLng mCurrentLatLng;// 当前经纬度坐标

    private PoiSearch mPoiSearch;// poi检索核心类
    private MySearchResultListener mySearchResultListener;// poi检索核心接口
    private Handler handler;//子线程结果接受器
    private Thread databaseThread;//通过子线程执行数据库操作

    PoiResult poiResultT;//存放poi搜索结果
    MyPoiOverlay poiOverlayT;//存放地图上的点
    String username;//登录的用户名


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 在使用SDK各组件之前初始化context信息,传入ApplicationContext
        // 该方法要在setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_find_my_and_other);
        init();
        // 设置是否允许定位图层,只有先允许定位图层后设置定位数据才会生效
        mBaiduMap.setMyLocationEnabled(true);
        location();
        mMapStatus = new MapStatus.Builder().zoom(15)
                .target(mCurrentLatLng).build();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory
                .newMapStatus(mMapStatus));
        btn_search.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                search();
            }
        });

        btn_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location();
/*                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);// 开启卫星图*/
                mBaiduMap.setTrafficEnabled(true);
                mBaiduMap.setBaiduHeatMapEnabled(true);
                // 获得地图的当前状态的信息
                mMapStatus = new MapStatus.Builder().zoom(15)
                        .target(mCurrentLatLng).build();
                // 设置地图将要变成的状态
                mMapStatusUpdate = MapStatusUpdateFactory
                        .newMapStatus(mMapStatus);
                mBaiduMap.setMapStatus(mMapStatusUpdate);// 设置地图的变化
            }
        });
    }
    private void init() {
        // 获得地图控件引用
        mMapView = (MapView) findViewById(R.id.bs_bmapView);
        // 获得地图控制器
        mBaiduMap = mMapView.getMap();// MapView与BaiduMap一一对应
        // 定位核心类
        mLocationClient = new LocationClient(getApplicationContext());
        // 定位回调接口
        myLocationListener = new MyLocationListener();
        // 定位按钮
        btn_location = (Button) findViewById(R.id.location);
        //poi搜索核心类
        mPoiSearch = PoiSearch.newInstance();
        //poi搜索回调接口
        mySearchResultListener = new MySearchResultListener();
        // 搜索按钮
        btn_search = (Button) findViewById(R.id.search);
        Intent intent = this.getIntent();
        username = intent.getStringExtra("username");
    }

    /**
     * 定位
     */
    private void location() {
        // 设置mLocationClient数据,如是否打开GPS,使用LocationClientOption类.
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(3000);// 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
        option.setOpenGps(true);// 打开GPS
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(myLocationListener);
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
            // 获取服务器回传的当前经纬度
            mCurrentLatLng = new LatLng(location.getLatitude(),
                    location.getLongitude());
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())// 获取定位精度
                    .latitude(location.getLatitude())// 获取纬度坐标
                    .longitude(location.getLongitude())// 获取精度坐标
                    .build();
            mBaiduMap.setMyLocationData(locData);// 设置定位数据

        }
    }


    /**
     * poi检索(圆形)
     */
    private void search() {
        mPoiSearch.setOnGetPoiSearchResultListener(mySearchResultListener);
        // PoiSearch需要设置相关参数,比如关键字,距离等
        PoiNearbySearchOption pnso = new PoiNearbySearchOption();
        pnso.keyword("美食");
        pnso.location(mCurrentLatLng);
        pnso.radius(1000);
        mPoiSearch.searchNearby(pnso);
    }

    public class MySearchResultListener implements OnGetPoiSearchResultListener {

        @Override
        public void onGetPoiDetailResult(final PoiDetailResult poiDetailResult) {
            if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
                // 检索失败
            } else {
                // 点击marker showInfoWindow
                mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                    private BitmapDescriptor descriptor;

                    /**
                     * 点击地图Marker事件
                     * @param arg0
                     * @return
                     */
                    @Override
                    public boolean onMarkerClick(Marker arg0) {
                        if(arg0.getExtraInfo().get("name")!=null){
                            Button btn = new Button(getApplicationContext());
                            btn.setBackgroundColor(0xAA00FF00);
                            final String myName=arg0.getExtraInfo().get("name").toString();
                            btn.setText(myName);
                            descriptor = BitmapDescriptorFactory.fromView(btn);
                            InfoWindow mInfoWindow = new InfoWindow(descriptor,
                                    arg0.getPosition(), -60,
                                    new InfoWindow.OnInfoWindowClickListener() {

                                        public void onInfoWindowClick() {
                                            // 点击infoWindow可以触发二次检索,跳转界面
                                            new AlertDialog.Builder(FindMyAndOther.this).setTitle("来啊！找吃的！提示您")//设置对话框标题
                                                    .setMessage("是否进入店铺详情页面")//设置显示的内容
                                                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Intent intent = new Intent();
                                                            intent.putExtra("username", username);
                                                            intent.putExtra("business_name",myName);
                                                            intent.setClass(FindMyAndOther.this, SeeDetailActivity.class);
                                                            FindMyAndOther.this.startActivity(intent);

                                                        }
                                                    })
                                                    .setNegativeButton("再看看", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Toast.makeText(getApplicationContext(), ""+myName,
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).show();
                                            mBaiduMap.hideInfoWindow();
                                        }
                                    });
                            mBaiduMap.showInfoWindow(mInfoWindow);
                            return false;
                        }
                        else{
                            // 设置弹窗 (View arg0, LatLng arg1, int arg2) y 偏移量 ，
                            Button btn = new Button(getApplicationContext());
                            btn.setBackgroundColor(0xAA00FF00);
                            btn.setText(poiDetailResult.getName());
                            // btn 变成 View 图片
                            descriptor = BitmapDescriptorFactory.fromView(btn);

                            InfoWindow mInfoWindow = new InfoWindow(descriptor,
                                    poiDetailResult.getLocation(), -60,
                                    new InfoWindow.OnInfoWindowClickListener() {

                                        public void onInfoWindowClick() {
                                            // 点击infoWindow可以触发二次检索,跳转界面
                                            new AlertDialog.Builder(FindMyAndOther.this).setTitle("系统提示")//设置对话框标题
                                                    .setMessage("是否提交")//设置显示的内容
                                                    .setPositiveButton("提交", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Intent intent = new Intent();
                                                            intent.putExtra("username", username);
                                                            intent.putExtra("business_name",poiDetailResult.getName());
                                                            intent.setClass(FindMyAndOther.this, SeeDetailActivity.class);
                                                            FindMyAndOther.this.startActivity(intent);

                                                        }
                                                    })
                                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Toast.makeText(getApplicationContext(), ""+poiDetailResult.getName(),
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).show();

                                            mBaiduMap.hideInfoWindow();
                                        }
                                    });
                            mBaiduMap.showInfoWindow(mInfoWindow);
                            return false;
                        }
                    }
                });

            }
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }

        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            // 首先判断检索结果是否有错,在判断检索结果是否为空
            if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                if (poiResult != null) {
                    mBaiduMap.clear();
                    // 绑定Overlay
                    MyPoiOverlay poiOverlay = new MyPoiOverlay(mBaiduMap);
                    poiOverlay.findDataBasePoi();
                    mBaiduMap.setOnMarkerClickListener(poiOverlay);
                    // 设置数据到overlay
                    poiResultT=poiResult;
                    poiOverlayT=poiOverlay;
                }

            } else {
                Toast.makeText(getApplicationContext(), "周边没有哦~",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void incando(PoiResult poiResult, MyPoiOverlay poiOverlay){
        poiOverlay.setData(poiResult);
        poiOverlay.addToMap();
        // 缩放地图，使所有Overlay都在合适的视野内 注： 该方法只对Marker类型的overlay有效
        poiOverlay.zoomToSpan();
    }
    // 自定义PoiOverlay
    class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
                    .poiUid(poi.uid));
            return super.onPoiClick(index);
        }

        /**
         * 执行数据库操作（查找数据库中存放的）
         * @param con
         * @throws java.sql.SQLException
         */
        @Override
        public void findpoi(Connection con) throws java.sql.SQLException {
            int temp=0;
            String sql = "select * from business";
            PreparedStatement pstmt =null;
            pstmt= con.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            int i=0;
            size=0;
            while(rs.next()){
                jingdu[i]=Double.parseDouble(rs.getString("business_jingdu"));
                weidu[i]=Double.parseDouble(rs.getString("business_weidu"));
                name[i]=rs.getString("business_name");
                i++;
                temp=1;
                size++;
            }
            try {
                pstmt.close();
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
            con.close();
            if(temp==1){
                Message message = new Message();
                message.what = 0x345;
                message.obj = "yes";
                //消息从子线程发回主线程
                handler.sendMessage(message);
            }
            super.findpoi(con);
        }

        @Override
        public void findDataBasePoi() {
            databaseThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Connection con = null;
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        con = DriverManager.getConnection("jdbc:mysql://120.78.185.195/baidumap", "root", "root");
                        findpoi(con);
                    } catch (ClassNotFoundException e) {
                        System.out.println("加载驱动程序出错");
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            });
            handler = new Handler() {
                public void handleMessage(Message msg) {
                    if (msg.what == 0x345) {
                        if (msg.obj.equals("yes")) {
                            Toast.makeText(FindMyAndOther.this,"yes",Toast.LENGTH_SHORT);
                            insert();
                            incando(poiResultT,poiOverlayT);
                        }
                        else{
                            incando(poiResultT,poiOverlayT);
                        }
                    }
                }
            };
            databaseThread.start();
            super.findDataBasePoi();
        }
    }

    @Override
    protected void onStart() {
        location();
        mLocationClient.start();// 开启定位请求
        super.onStart();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mLocationClient.stop();// 停止定位
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }
    // 捕获返回键的方法2
    @Override
    public void onBackPressed() {
        this.finish();
        databaseThread.interrupt();
        mMapView.onDestroy();
        Intent intent = new Intent();
        intent.setClass(FindMyAndOther.this, LoginActivity.class);
        FindMyAndOther.this.startActivity(intent);
    }
}