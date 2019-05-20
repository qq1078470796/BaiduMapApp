package com.example.asuspc.entity;

/**
 * Created by asus   pc on 2017/12/3.
 */

/**
 * 用户预约清单
 */
public class BusinessList {
    private String userName;//下订单用户
    private String businessName;//商家名字
    private int payType;//支付状态（0 未支付，1已支付，2已完成）

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }
}
