package com.example.asuspc.entity;

/**
 * Created by asus   pc on 2017/12/1.
 */

/**
 * 堂食套餐类
 */
public class Dining {
    private String dining_id;//堂食套餐id
    private String dinging_name;//堂食套餐名称
    private double dining_price;//堂食套餐价格
    private String dining_comeform;//堂食套餐属于哪一个商户
    private byte[] dining_picture;//堂食套餐宣传图序列

    public String getDining_id() {
        return dining_id;
    }

    public void setDining_id(String dining_id) {
        this.dining_id = dining_id;
    }

    public String getDinging_name() {
        return dinging_name;
    }

    public void setDinging_name(String dinging_name) {
        this.dinging_name = dinging_name;
    }

    public double getDining_price() {
        return dining_price;
    }

    public void setDining_price(double dining_price) {
        this.dining_price = dining_price;
    }

    public String getDining_comeform() {
        return dining_comeform;
    }

    public void setDining_comeform(String dining_comeform) {
        this.dining_comeform = dining_comeform;
    }

    public byte[] getDining_picture() {
        return dining_picture;
    }

    public void setDining_picture(byte[] dining_picture) {
        this.dining_picture = dining_picture;
    }
}
