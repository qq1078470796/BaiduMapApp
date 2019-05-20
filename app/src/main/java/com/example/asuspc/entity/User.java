package com.example.asuspc.entity;

/**
 * Created by asus   pc on 2017/12/1.
 */

/**
 * 使用者基础类
 */
public class User {
    private String username;//登录名(主键)
    private String password;//密码
    private int type;//登陆者类型（0代表管理员 ，1代表普通商户)
    private String phone;//登陆者联系方式

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
