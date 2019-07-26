package com.wang.faceidtest2.DataBase.DataAccessObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wang.faceidtest2.DataBase.DataBaseOpenHelper.UserDBOpenHelper;
import com.wang.faceidtest2.Services.User;

/**
 * @version $Rev$
 * @auther wangyi
 * @des ${TODO}
 * @updateAuther $Auther$
 * @updateDes ${TODO}
 */
public class UserDao {
    private UserDBOpenHelper mUserDBOpenHelper;
    public UserDao(Context context){
        mUserDBOpenHelper = new UserDBOpenHelper(context);
    }

    public long save(String name,String password){
        SQLiteDatabase db=mUserDBOpenHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("username",name);
        values.put("password",password );
        long result=db.insert("user", null,values );
        db.close();
        return result;
    }

    /**
     * 删除匹配的
     * @param name
     * @return
     */

    public int delete(String name){
        SQLiteDatabase db=mUserDBOpenHelper.getWritableDatabase();
        int result=db.delete("user", "username=?",new String[]{name} );
        db.close();
        return result;
    }

    /**
     * 删除所有数据
     * @return
     */
    public int delete(){
        SQLiteDatabase db=mUserDBOpenHelper.getWritableDatabase();
        int result=db.delete("user", null,null);
        db.close();
        return result;
    }

    /**
     * 查找员工
     * @param id 员工账号（工号）
     * @return
     */
    public User find(String id){
        User user =new User();
        SQLiteDatabase db=mUserDBOpenHelper.getReadableDatabase();
        Cursor cursor=db.query("user",new String[]{"username","password"},null,null,null,null,null);
        return user;
    }

}
