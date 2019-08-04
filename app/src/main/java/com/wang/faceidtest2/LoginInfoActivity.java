package com.wang.faceidtest2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wang.faceidtest2.Services.InfoItem;
import com.wang.faceidtest2.Services.LoginStatus;
import com.wang.faceidtest2.Services.User;

import java.util.ArrayList;
import java.util.List;

public class LoginInfoActivity extends AppCompatActivity {
    private List<InfoItem> mInfoItems;
    private ListView lv;
    private User mUser=new User();
    private final String TAG = "LoginInfoActivity";
    private CommonAdapter adapter;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_info);
        lv=findViewById(R.id.lv);


        mInfoItems = new ArrayList<InfoItem>();
        InfoItem item1 = new InfoItem();
        item1.setStatus(LoginStatus.LATE);
        item1.setTime("yes");
        InfoItem item2 = new InfoItem();
        item2.setStatus(LoginStatus.ONTIME);
        item2.setTime("ok");
        mInfoItems.add(item1);
        mInfoItems.add(item2);
        mUser.setLoginInfos(mInfoItems);


        if (adapter==null){
            adapter=new CommonAdapter();
            lv.setAdapter(adapter);
        }else{
            //通知数据适配器更新数据，而不是new新的数据适配器
            adapter.notifyDataSetChanged();
            lv.setAdapter(adapter);
        }
    }
    private class CommonAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mInfoItems.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView==null){
                view = View.inflate(LoginInfoActivity.this,R.layout.infoitem,null);
            }else {
                view = convertView;
            }
            final TextView tv_infoitem_time = view.findViewById(R.id.time);
            final TextView tv_infoitem_status = view.findViewById(R.id.status);
            final InfoItem item=mInfoItems.get(mInfoItems.size()-position-1);

            tv_infoitem_time.setText(mUser.getLoginInfos().get(position).getTime());
            tv_infoitem_status.setText(mUser.getLoginInfos().get(position).getStatus().toString());
            return view;
        }
    }
}
