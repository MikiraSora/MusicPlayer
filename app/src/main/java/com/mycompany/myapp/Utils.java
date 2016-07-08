package com.mycompany.myapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by mikir on 2016/7/2.
 */
public class Utils {
    /*
    根据文件路径作为缓存名获取或者加载文件的封面
     */
    public static Bitmap getCover(String path){
        try{
            byte[] buffer=Cache.getCacheData(path, new Cache.OnRequestFile() {
                @Override
                public byte[] onRequestFile(String abs_path) {
                    Log.i("Utils","Using dym-pic in mp3 file : "+abs_path);
                    SongInfoParser parser = new SongInfoParser(abs_path);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    parser.getCover().compress(Bitmap.CompressFormat.PNG, 100, baos);
                    return baos.toByteArray();
                }
            });
            return BitmapFactory.decodeByteArray(buffer,0,buffer.length);
        }catch (Exception e){
            return null;
        }
    }

    /*
    获取缓存文件的输入流
     */
    public static FileInputStream getCoverStream(String path){
        try{
            FileInputStream buffer=Cache.getCacheStream(path, new Cache.OnRequestFile() {
                @Override
                public byte[] onRequestFile(String abs_path) {
                    SongInfoParser parser = new SongInfoParser(abs_path);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    parser.getCover().compress(Bitmap.CompressFormat.PNG, 100, baos);
                    return baos.toByteArray();
                }
            });
            return buffer;
        }catch (Exception e){
            return null;
        }
    }
}
