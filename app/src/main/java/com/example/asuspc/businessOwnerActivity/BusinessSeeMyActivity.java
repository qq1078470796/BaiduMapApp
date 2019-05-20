package com.example.asuspc.businessOwnerActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.asuspc.ManageActivity.BaseActivity;
import com.example.asuspc.baidumapapp.R;
import com.example.asuspc.entity.Business;

public class BusinessSeeMyActivity extends Activity implements BaseActivity {

    private MapView mMapView;// 地图视图
    private BaiduMap mBaiduMap;// 地图控制器 setMapStatus(mMapStatusUpdate);
    private MapStatus mMapStatus;// 地图当前状态
    private MapStatusUpdate mMapStatusUpdate;// 地图将要变化成的状态
    private Button bs_return;
    String username;
    private LatLng mCurrentLatLng;// 当前经纬度坐标

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_see_my);
        init();
        addEvent();
    }

    @Override
    public void init() {
        // 获得地图控件引用
        mMapView = (MapView) findViewById(R.id.bs_bmapView);
        // 获得地图控制器
        mBaiduMap = mMapView.getMap();// MapView与BaiduMap一一对应
        mBaiduMap.setMyLocationEnabled(true);
        mCurrentLatLng=new LatLng(Double.parseDouble(getIntent().getStringExtra("weidu"))
                ,Double.parseDouble(getIntent().getStringExtra("jingdu")));
        username=getIntent().getStringExtra("username");
        mBaiduMap.setTrafficEnabled(true);
        mBaiduMap.setBaiduHeatMapEnabled(true);
        // 获得地图的当前状态的信息
        mMapStatus = new MapStatus.Builder().zoom(15)
                .target(mCurrentLatLng).build();
        // 设置地图将要变成的状态
        mMapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mMapStatus);
        mBaiduMap.setMapStatus(mMapStatusUpdate);// 设置地图的变化
        bs_return= (Button) findViewById(R.id.bs_return);

    }

    @Override
    public void addEvent() {
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.huaji);
        //构建MarkerOption，用于在地图上添加Marker
        MarkerOptions option = new MarkerOptions()
                .position(mCurrentLatLng)
                .icon(bitmap);
        mBaiduMap.addOverlay(option);
        bs_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("username", username);
                intent.setClass(BusinessSeeMyActivity.this, ManagerActivity.class);
                BusinessSeeMyActivity.this.startActivity(intent);
            }
        });

    }

}
