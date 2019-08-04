package com.wang.faceidtest2.Services;

import java.util.List;

/**
 * @version $Rev$
 * @auther wangyi
 * @des ${TODO}
 * @updateAuther $Auther$
 * @updateDes ${TODO}
 */
public class User {
    private String id;//登录用账号
    private String pwd;//密码
    private String email;//邮箱
    private String name;//用户姓名
    private List<InfoItem> mLoginInfos;

    public List<InfoItem> getLoginInfos() {
        return mLoginInfos;
    }

    public void setLoginInfos(List<InfoItem> loginInfos) {
        mLoginInfos = loginInfos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
