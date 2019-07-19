package com.wang.faceidtest2;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.faceidtest2.Utils.LBSUtils;

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

public class MainActivity extends AppCompatActivity {
    private static final int TAKE_PHOTO = 1;
    private static final int LOGIN_SUCCESS = 2;
    private static final int LOGIN_ERROR = 3;
    private static final String TAG = "MainActivity";
    private TextView locationMsg;
    private Uri imageUri;
    private static final String ServerIP = "http://10.0.2.2:8888";
    //private ImageView picture;
    private ProgressDialog mProgressDialog;
    private String imagePath_take;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏显示（隐藏状态栏）*/
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("正在识别，请稍后...");
        mProgressDialog.setCancelable(false);
        Button button = findViewById(R.id.id_login);
        locationMsg = findViewById(R.id.location);
        //picture = findViewById(R.id.picture);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //权限检查代码
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,//指定GPS定位的提供者
                1000,//间隔时间
                1,//位置间隔1米
                new LocationListener() {//监听GPS定位信息是否改变
                    @Override
                    public void onLocationChanged(Location location) {//GPS信息改变时回调

                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {//GPS状态改变时回调

                    }

                    @Override
                    public void onProviderEnabled(String s) {//定位提供者启动时回调

                    }

                    @Override
                    public void onProviderDisabled(String s) {//定位提供者关闭时回调

                    }
                }
        );
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);//回去最新的定位信息
        locationMsg.setText(LBSUtils.locationUpdates(location));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String imagename="face.jpg";
                File outputImage=new File(getExternalCacheDir(),imagename);//创建File对象，用于存储拍照后的照片
                imagePath_take=getExternalCacheDir()+"/"+imagename;
                try{
                    if (outputImage.exists()){
                        outputImage.delete();//文件存在，就删除文件
                    }
                    outputImage.createNewFile();//创建新文件
                }catch (IOException e){
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT>=24){//android7.0以上
                    imageUri= FileProvider.getUriForFile(MainActivity.this,"com.wang.faceidtest2" , outputImage);
                }else{//android7.0以下
                    imageUri= Uri.fromFile(outputImage);
                }
                //启动相机程序
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri );
                intent.putExtra("camerasensortype", 2); // 调用前置摄像头
                intent.putExtra("autofocus", true); // 自动对焦
                intent.putExtra("fullScreen", false); // 全屏
                intent.putExtra("showActionIcons", false);
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK){
                    try{
                        mProgressDialog.show();
                        /*Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);*/
                        File file = new File(imagePath_take);
                        uploadImage(MainActivity.this,ServerIP,file);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                    break;
        }
    }


    private void uploadImage(final Context context, String url, File file) {
        try {
            OkHttpClient mOkHttpClient = new OkHttpClient();
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("img", file.getName(),
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
                    Log.i(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "上传失败！", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.i(TAG, "上传成功！");
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           Toast.makeText(context, "上传成功", Toast.LENGTH_SHORT).show();
                       }
                   });

                }
            });
            //Toast.makeText(context, "上传成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,e.getMessage());
            Toast.makeText(context, "上传失败!"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }finally {
            mProgressDialog.dismiss();
        }
    }



}
