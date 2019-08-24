package com.wang.faceidtest2.Services;

/**
 * @version $Rev$
 * @auther wangyi
 * @des ${TODO}
 * @updateAuther $Auther$
 * @updateDes ${TODO}
 */
public class InfoItem {
    private Status.LoginStatus status;
    private String time;

    public Status.LoginStatus getStatus() {
        return status;
    }

    public void setStatus(Status.LoginStatus status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
