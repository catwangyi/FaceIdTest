package com.wang.faceidtest2;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.wang.faceidtest2.HttpUtils.HttpUtil;
import com.wang.faceidtest2.Services.RunOnUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;
import top.zibin.luban.OnRenameListener;

public class MainActivity extends AppCompatActivity {
    private static final int TAKE_PHOTO = 1;
    private static final int LOGIN_SUCCESS = 2;
    private static final int LOGIN_ERROR = 3;
    private static final String TAG = "MainActivity";
    private Uri imageUri;

    private ProgressDialog mProgressDialog;
    private String imagePath_take;
    public LocationClient mLocationClient;
    private MapView mMapView;
    private BaiduMap baiduMap;
    private FloatingActionButton mFloatingActionButton;
    private boolean isFirstLocate = true;//用来显示是否是第一次定位
    private DrawerLayout mDrawerLayout;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());//这句话需要加在setContentView之前
        setContentView(R.layout.activity_main);
        Toolbar toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);//禁止手动滑动弹出
        //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);//打开手动滑动弹出
        ActionBar actionBar = getSupportActionBar();
        final Intent intent=getIntent();
        username = (String)intent.getSerializableExtra("Name");
        final NavigationView navigationView = findViewById(R.id.nav_view);//NavigationView

        //设置Header内容
        View headerView= navigationView.getHeaderView(0);
        TextView username_tv = headerView.findViewById(R.id.username);
        username_tv.setText(username);
        TextView userphone_tv = headerView.findViewById(R.id.phone);
        userphone_tv.setText((String)intent.getSerializableExtra("Phone"));
        Log.i(TAG+"名字", username);
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);//菜单按钮
        }

        //navigationView.setCheckedItem(R.id.nav_log);//设置默认选项
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {//处理按钮事件；
                switch (menuItem.getItemId()){
                    case R.id.person_details:
                        //Toast.makeText(getApplicationContext(),"个人资料！" ,Toast.LENGTH_SHORT).show();
                        //Intent intent1 = new Intent(getApplicationContext(),LoginInfoActivity.class);
                        //startActivity(intent1);
                        break;
                    case R.id.nav_log://最近记录
                        //Toast.makeText(getApplicationContext(),"最近记录！" ,Toast.LENGTH_SHORT).show();
                        Intent intent2 = new Intent(getApplicationContext(), CheckInfoActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.logout://退出
                        Intent intent3 = new Intent(getApplicationContext(),LoginActivity.class);
                        startActivity(intent3);
                        //Toast.makeText(getApplicationContext(),"退出！" ,Toast.LENGTH_SHORT ).show();
                        finish();
                        break;
                }
                //关闭滑动菜单
                //mDrawerLayout.closeDrawers();
                return true;
            }
        });

        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("正在上传图片，请稍后...");
        mFloatingActionButton = findViewById(R.id.login);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        mMapView= findViewById(R.id.bdmapView);
        //隐藏baidu的logo
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)){
            child.setVisibility(View.INVISIBLE);
        }
        mMapView.showZoomControls(false);//放大缩小控件
        mMapView.showScaleControl(true);//比例尺控件

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


        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(),"点击!" ,Toast.LENGTH_SHORT).show();
                String imagename = username+".jpg";
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
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });
    }


    private void requestLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(1000);
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll");//需要设置坐标偏移标准，否则定位不准确,后面是字母L
        mLocationClient.setLocOption(option);
        mLocationClient.start();//打开地图定位图层
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.refresh:
                isFirstLocate = true;
                mLocationClient.start();//重新定位
                break;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    private void navigateTo(BDLocation location){
        if(location!=null){
            if (isFirstLocate){
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude() );
                Log.i(TAG, "经度："+location.getLatitude());
                Log.i(TAG, "纬度:"+location.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
                baiduMap.animateMapStatus(update);
                update = MapStatusUpdateFactory.zoomTo(18f);
                baiduMap.animateMapStatus(update);//设置地图位置
                isFirstLocate = false;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection())
                    .latitude(location.getLatitude())//纬度
                    .longitude(location.getLongitude())//经度
                    .build();
            baiduMap.setMyLocationData(locData);
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
            case TAKE_PHOTO://拍照
                if (resultCode == RESULT_OK){
                    try{
                        mProgressDialog.show();
                        File file = new File(imagePath_take);
                        resize(file);//压缩图片

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                    break;
        }
    }



    /**
     * 定位
     */
    public class MyLocationListener extends BDAbstractLocationListener {
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {

        if (bdLocation.getLocType()==BDLocation.TypeGpsLocation||bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
            navigateTo(bdLocation);

        }else {
            if (!isFirstLocate){
                Toast.makeText(getApplicationContext(), "发生未知错误，定位失败！", Toast.LENGTH_SHORT).show();
                Log.i(TAG,"发生未知错误，定位失败！" );
            }
        }
    }
}
    /**
     * 压缩图片
     *
     */
    public void resize(final File src) {
        Luban.with(getApplicationContext()).load(src).ignoreBy(1000).setTargetDir(src.getParent()).setRenameListener(new OnRenameListener() {
            @Override
            public String rename(String filePath) {
                return username+".jpg";
            }
        }).setCompressListener(new OnCompressListener() {
            @Override
            public void onStart() {
                Log.i(TAG,"开始压缩" );
                Log.i(TAG,"图片位置："+src.getPath());
            }

            @Override
            public void onSuccess(final File file) {
                Log.i(TAG,"压缩成功" );
                    HttpUtil.uploadImg(file,getResources().getString(R.string.upload_addr), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            RunOnUI.Run(getApplicationContext(),"上传失败"+e.getMessage());
                            Log.i(TAG,"上传失败"+e.getMessage());
                            mProgressDialog.dismiss();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.i(TAG,"上传成功");
                            mProgressDialog.dismiss();
                        }
                    });
            }

            @Override
            public void onError(Throwable e) {
                final File file = new File(imagePath_take);
                Log.i(TAG,"压缩失败"+e.getMessage());
                HttpUtil.uploadImg(file, getResources().getString(R.string.upload_addr), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        RunOnUI.Run(getApplicationContext(),"上传失败"+e.getMessage());
                        Log.i(TAG,"上传失败"+e.getMessage());
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i(TAG,"上传成功");
                        mProgressDialog.dismiss();
                    }
                });
            }
        }).launch();
    }

}
