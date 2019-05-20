package com.example.asuspc.entity;

/**
 * Created by asus   pc on 2017/12/1.
 */


/**
 * 商家类
 */
public class Business {

    private String business_id;//商家id
    private String business_name;//商家名称
    private String business_address;//商家的详细地址
    private String business_phone;//商家的联系方式
    private int business_usernum;//堂食内剩余座位(-1代表无座位数限制)
    private String business_owner;//该商家的用户
    private byte[] business_picture;//商家门户图
    private String business_jingdu;//商家地图的经度
    private String business_weidu;//商家地图的纬度

    public String getBusiness_jingdu() {
        return business_jingdu;
    }

    public void setBusiness_jingdu(String business_jingdu) {
        this.business_jingdu = business_jingdu;
    }

    public String getBusiness_weidu() {
        return business_weidu;
    }

    public void setBusiness_weidu(String business_weidu) {
        this.business_weidu = business_weidu;
    }

    public String getBusiness_id() {
        return business_id;
    }

    public void setBusiness_id(String business_id) {
        this.business_id = business_id;
    }

    public String getBusiness_name() {
        return business_name;
    }

    public void setBusiness_name(String business_name) {
        this.business_name = business_name;
    }

    public String getBusiness_address() {
        return business_address;
    }

    public void setBusiness_address(String business_address) {
        this.business_address = business_address;
    }

    public String getBusiness_phone() {
        return business_phone;
    }

    public void setBusiness_phone(String business_phone) {
        this.business_phone = business_phone;
    }


    public int getBusiness_usernum() {
        return business_usernum;
    }

    public void setBusiness_usernum(int business_usernum) {
        this.business_usernum = business_usernum;
    }

    public String getBusiness_owner() {
        return business_owner;
    }

    public void setBusiness_owner(String business_owner) {
        this.business_owner = business_owner;
    }

    public byte[] getBusiness_picture() {
        return business_picture;
    }

    public void setBusiness_picture(byte[] business_picture) {
        this.business_picture = business_picture;
    }
}
