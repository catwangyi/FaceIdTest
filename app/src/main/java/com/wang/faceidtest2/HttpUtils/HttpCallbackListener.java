package com.wang.faceidtest2.HttpUtils;

/**
 * @version $Rev$
 * @auther wangyi
 * @des ${TODO}
 * @updateAuther $Auther$
 * @updateDes ${TODO}
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
