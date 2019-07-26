package com.wang.faceidtest2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.wang.faceidtest2.Services.User;

public class LoginActivity extends AppCompatActivity {
    private Button login;
    private Button exit;
    private EditText et_id_login;
    private EditText et_pwd_login;
    private String id;
    private String pwd;
    private User user;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private CheckBox mCheckBox;
    private final String TAG="LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.login_ac);
        et_id_login = findViewById(R.id.et_id_login);
        exit = findViewById(R.id.exit);
        et_pwd_login = findViewById(R.id.et_pwd_login);
        mCheckBox = findViewById(R.id.remeber_pwd);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isRemeber = mPreferences.getBoolean("remeber_pwd",false);
        if (isRemeber){
            //将账号和密码都设置到文本框中
            String id = mPreferences.getString("id","" );
            String pwd = mPreferences.getString("pwd","" );
            et_id_login.setText(id);
            et_pwd_login.setText(pwd);
            mCheckBox.setChecked(true);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                id = et_id_login.getText().toString();
                pwd = et_pwd_login.getText().toString();

                if (id.isEmpty()||pwd.isEmpty()){//输入不合法
                    Toast.makeText(getApplicationContext(),"账号或密码不能为空！" ,Toast.LENGTH_SHORT).show();
                }else if(id.length()>20||pwd.length()>20){//输入错误
                    Toast.makeText(getApplicationContext(),"请输入正确的账号和密码！" ,Toast.LENGTH_SHORT).show();
                }else{//输入合法
                    //开始上传账号密码


                    //得到返回的登录结果
                    if("用户名不存在".isEmpty()){
                        Toast.makeText(getApplicationContext(),"用户名不存在！" ,Toast.LENGTH_SHORT).show();
                    }else if("密码错误".isEmpty()){
                        Toast.makeText(getApplicationContext(),"密码错误！" ,Toast.LENGTH_SHORT).show();
                    }else{
                        mEditor = mPreferences.edit();
                        if (mCheckBox.isChecked()){
                            //复选框被选中
                            mEditor.putBoolean("remeber_pwd", true);
                            mEditor.putString("id",id );
                            mEditor.putString("pwd",pwd );
                        }else {
                            mEditor.clear();
                        }
                        mEditor.apply();
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                }
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
