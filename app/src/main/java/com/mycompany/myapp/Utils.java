package com.mycompany.myapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.mycompany.myapp.CacheCollection.Cache;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

/**
 * Created by mikir on 2016/7/2.
 */
public class Utils {
    /*
    根据文件路径作为缓存名获取或者加载文件的封面
     */
    public static Bitmap getCover(String path){
        try{
            byte[] buffer= Cache.getCacheData(getCoverName(path), new Cache.OnRequestFile() {
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

    public static Bitmap getBlurCover(final String path){
        try{
            byte[] buffer=Cache.getCacheData(getBlurCoverName(path), new Cache.OnRequestFile() {
                @Override
                public byte[] onRequestFile(String abs_path) {
                    Log.i("RenderBG","using Dym-Blur picture");
                    try {
                        Bitmap src=getCover(path);
                        Bitmap bmp = Blur.DoBlurWithScale(src, 25.0f, 0.4f);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.PNG,50, baos);
                        byte[] buffer = baos.toByteArray();
                        return buffer;
                    }catch (Exception e){
                        //TODO
                    }
                    return null;
                }
            });
            return BitmapFactory.decodeByteArray(buffer,0,buffer.length);
        }catch (Exception e){
            return null;
        }
    }

    public static ExecutorService getCacheThreadPool(Context context){return ((ParameterSender)context.getApplicationContext()).getCacheThreadPoor();}

    public static String getCoverName(String path){return path;}
    public static String getBlurCoverName(String path){return path+"_r";}
}
