package com.mycompany.myapp;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mikir on 2016/6/28.
 */
public class Cache {
    private static Context ctx=null;

    private static CacheDataBase cache=null;

    public static void Init(Context _ctx){
        ctx=_ctx;
        File file=new File(ctx.getExternalCacheDir().getAbsoluteFile()+".nomedia");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cache=new CacheDataBase(ctx);
    }

    public static byte[] getCacheData(String abs_path,OnRequestFile failedCallback)throws Exception{
        String cacheId=cache.getID(abs_path);
        File cacheFile=getCacheFile(cacheId);
        if(!cacheFile.exists()){
            byte[] buffer= failedCallback!=null?failedCallback.onRequestFile(abs_path):null;
            saveCacheData(abs_path, buffer);
            return buffer;
        }
        FileInputStream fileInputStream=new FileInputStream(cacheFile);
        byte[] buffer=new byte[fileInputStream.available()];
        fileInputStream.read(buffer);
        fileInputStream.close();
        return buffer;
    }

    public static boolean isExsit(String abs_path){
        String cacheId=Integer.toString(cache.saveFile(abs_path));
        File cacheFile=new File(ctx.getExternalCacheDir()+cacheId);
        return cacheFile.exists();
    }

    public static void saveCacheData(String abs_path,byte[] data){
        if(data==null)
            data=new byte[1];
        String cacheId=Integer.toString(cache.saveFile(abs_path));
        File cacheFile=getCacheFile(cacheId);
        try{
            if (!cacheFile.exists())
                cacheFile.createNewFile();
            FileOutputStream fileOutputStream=new FileOutputStream(cacheFile,false);
            fileOutputStream.write(data);
            fileOutputStream.close();
        }catch (Exception e){
            Log.e("Cache",e.getMessage());
        }
    }

    private static File getCacheFile(String cacheId){
        return new File(ctx.getExternalCacheDir().getAbsoluteFile()+"/"+cacheId+".cache");
    }

    public static interface OnRequestFile{
        byte[] onRequestFile(String abs_path);
    }

    private static class CacheDataBase extends SQLiteOpenHelper{

        private String file_id_reflection="file";
        private int id=1;

        public CacheDataBase(Context context){
            super(context,"cache", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("create table if not exists " + file_id_reflection + "(AbsFile_Path primary key,ID)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
        }

        public String getID(String path){
            Cursor cursor = getReadableDatabase().query(file_id_reflection, null, "AbsFile_Path=?", new String[]{path}, null, null, null,"1");
            while (cursor.moveToNext()) {
                return cursor.getString(id);
            }
            return null;
        }

        public int saveFile(String path){
            if (getReadableDatabase().query(file_id_reflection, null, "AbsFile_Path=?", new String[]{path}, null, null, null, null).getCount() != 0)
                return Integer.parseInt(getID(path));
            ContentValues cvalue = new ContentValues();
            cvalue.put("AbsFile_Path", path);
            cvalue.put("ID", path.hashCode());
            getWritableDatabase().insert(file_id_reflection, "AbsFile_Path", cvalue);
            return path.hashCode();
        }
    }
}
