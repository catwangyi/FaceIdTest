package com.wang.faceidtest2;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private ProgressDialog mProgressDialog;
    private String imagePath_take;
    public LocationClient mLocationClient;
    private MapView mMapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏显示（隐藏状态栏）*/
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("正在识别，请稍后...");
        mProgressDialog.setCancelable(false);
        //Button button = findViewById(R.id.id_login);
        //locationMsg = findViewById(R.id.location);


        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mMapView= findViewById(R.id.bdmapView);
        baiduMap = mMapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
          String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions ,1 );
        }else{
            requestLocation();
        }


        /*button.setOnClickListener(new View.OnClickListener() {
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
        });*/
    }

    private void requestLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    private void navigageTo(BDLocation location){
        if(location!=null){
            if (isFirstLocate){
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude() );
                Log.i(TAG, "经度："+location.getLatitude());
                Log.i(TAG, "纬度:"+location.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
                baiduMap.animateMapStatus(update);
                update = MapStatusUpdateFactory.zoomTo(16f);
                baiduMap.animateMapStatus(update);
                isFirstLocate = false;
            }
            MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
            locationBuilder.latitude(location.getLatitude());
            locationBuilder.longitude(location.getLongitude());
            MyLocationData locationData = locationBuilder.build();
            baiduMap.setMyLocationData(locationData);
        }else{
            Toast.makeText(getApplicationContext(), "没有获取到定位信息！", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mMapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        mMapView.onPause();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result:grantResults){
                        if (result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(MainActivity.this,"必须同意所有的权限才能使用本程序！" ,Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(MainActivity.this,"未知错误！" ,Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
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




public class MyLocationListener implements BDLocationListener {

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        StringBuilder stringBuilder = new StringBuilder();
        if (bdLocation.getLocType()==BDLocation.TypeGpsLocation||bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
            navigageTo(bdLocation);
            stringBuilder.append("经度：")
                    .append(bdLocation.getLatitude())
                    .append("\n")
                    .append("纬度：")
                    .append(bdLocation.getLongitude())
                    .append("\n")
                    .append("定位方式：");
            if (bdLocation.getLocType()==BDLocation.TypeGpsLocation){
                stringBuilder.append("GPS");
            }else if(bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
                stringBuilder.append("网络");
            }
            Toast.makeText(getApplicationContext(), stringBuilder.toString(),Toast.LENGTH_SHORT).show();
            Log.i(TAG,"定位成功！" );
        }else {
            //locationMsg.setText("发生未知错误，定位失败！");
            Toast.makeText(getApplicationContext(), "发生未知错误，定位失败！", Toast.LENGTH_SHORT).show();
            Log.i(TAG,"发生未知错误，定位失败！" );
        }
    }
}

}
