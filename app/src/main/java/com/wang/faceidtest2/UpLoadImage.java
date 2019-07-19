package com.wang.faceidtest2;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @version $Rev$
 * @auther wangyi
 * @des ${TODO}
 * @updateAuther $Auther$
 * @updateDes ${TODO}
 */
public class UpLoadImage {
    public static void uploadImage(final Context context, String url, File file){
        try{
            OkHttpClient mOkHttpClient = new OkHttpClient();
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("img", "HeadPortrait.jpg",
                            RequestBody.create(MediaType.parse("image/png"), file));
            RequestBody requestBody = builder.build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(context,"上传失败！"+e.getMessage() ,Toast.LENGTH_SHORT ).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Toast.makeText(context,"上传成功",Toast.LENGTH_SHORT ).show();

                }
            });

        }catch (Exception e){
            e.printStackTrace();

        }
    }
}
