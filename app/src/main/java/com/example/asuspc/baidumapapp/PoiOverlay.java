package com.example.asuspc.baidumapapp;

import android.os.Bundle;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiResult;
import com.example.asuspc.entity.Dining;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于显示poi的overly
 */
public class PoiOverlay extends OverlayManager {

    private static final int MAX_POI_SIZE = 20;

    private PoiResult mPoiResult = null;
    List<OverlayOptions> dataBaseList = new ArrayList<OverlayOptions>();
    double[] jingdu=new double[100];double[] weidu=new double[100];
    String [] name=new String[100];
    int size=0;

    String [] poiName=new String[100];
    List<OverlayOptions> markerList;


    /**
     * 构造函数
     * 
     * @param baiduMap
     *            该 PoiOverlay 引用的 BaiduMap 对象
     */
    public PoiOverlay(BaiduMap baiduMap) {
        super(baiduMap);
    }

    public void findDataBasePoi(){

    }
    public void insert(){
        for(int i=0;i<size;i++){
            LatLng point = new LatLng(weidu[i],jingdu[i]);
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.huaji);
            //构建MarkerOption，用于在地图上添加Marker
            Bundle bundle = new Bundle();
            bundle.putString("name", name[i]);
            MarkerOptions option = new MarkerOptions()
                    .position(point).extraInfo(bundle)
                    .icon(bitmap);

            dataBaseList.add(option);
        }
    }
    public void findpoi(Connection con) throws SQLException {

    }
    /**
     * 设置POI数据
     * 
     * @param poiResult
     *            设置POI数据
     */
    public void setData(PoiResult poiResult) {
        this.mPoiResult = poiResult;
    }

    @Override
    public final List<OverlayOptions> getOverlayOptions() {
        if (mPoiResult == null || mPoiResult.getAllPoi() == null) {
            return null;
        }
        markerList = new ArrayList<OverlayOptions>();
        int markerSize = 0;
        for (int i = 0; i < mPoiResult.getAllPoi().size()
                && markerSize < MAX_POI_SIZE; i++) {
            if (mPoiResult.getAllPoi().get(i).location == null) {
                continue;
            }
            markerSize++;
            Bundle bundle = new Bundle();
            bundle.putInt("index", i);
            markerList.add(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromAssetWithDpi("Icon_mark"
                            + markerSize + ".png")).extraInfo(bundle)
                    .position(mPoiResult.getAllPoi().get(i).location));
            poiName[i]=mPoiResult.getAllPoi().get(i).name;
        }
        /**
         * 如果poi查询中的结果包含自定义结果，则不显示自定义标签
         */
        if(dataBaseList.size()!=0){
            for(int i=0;i<size;i++){
                int temp=0;
                for(int j=0;j<markerSize;j++){
                    if(poiName[j].equals(name[i])){
                        temp=1;break;
                    }
                }
                if(temp==0){
                    markerList.add(dataBaseList.get(i));
                }
            }
        }

        return markerList;
    }

    /**
     * 获取该 PoiOverlay 的 poi数据
     * 
     * @return
     */
    public PoiResult getPoiResult() {
        return mPoiResult;
    }

    /**
     * 覆写此方法以改变默认点击行为
     * 
     * @param i
     *            被点击的poi在
     *            {@link PoiResult#getAllPoi()} 中的索引
     * @return
     */
    public boolean onPoiClick(int i) {
//        if (mPoiResult.getAllPoi() != null
//                && mPoiResult.getAllPoi().get(i) != null) {
//            Toast.makeText(BMapManager.getInstance().getContext(),
//                    mPoiResult.getAllPoi().get(i).name, Toast.LENGTH_LONG)
//                    .show();
//        }
        return false;
    }

    @Override
    public final boolean onMarkerClick(Marker marker) {
        if (!mOverlayList.contains(marker)) {
            return false;
        }
        if (marker.getExtraInfo() != null) {
            return onPoiClick(marker.getExtraInfo().getInt("index"));
        }
        return false;
    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        // TODO Auto-generated method stub
        return false;
    }
}
