package com.wang.faceidtest2.Services;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

/**
 * @version $Rev$
 * @auther wangyi
 * @des ${TODO}
 * @updateAuther $Auther$
 * @updateDes ${TODO}
 */
public class RunOnUI {
    public static void Run(final Context context, final String msg){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(context,msg,Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }).start();
    }
}
