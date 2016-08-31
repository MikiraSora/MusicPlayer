package com.mycompany.myapp;

import android.app.Application;
import android.graphics.Bitmap;

import com.mycompany.myapp.CacheCollection.BitmapCache;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by mikir on 2016/7/8.
 */
public class ParameterSender extends Application {

    HashMap<Object,Object> map=new HashMap<>();

    //BitmapCache bitmapCache=new BitmapCache();

    ExecutorService CacheThreadPoor=null;
    //SoftReference<HashMap<String,Bitmap>> Cache=new SoftReference<>(new HashMap<String,Bitmap>());

    public Object getObject(Object key){
        return map.containsKey(key)?map.get(key):null;
    }

    public void putObject(Object key,Object val){
        map.put(key,val);
    }

    /*
    public HashMap<String,Bitmap> getCacheBitmapMap(){
        if(Cache.get()==null)
            Cache=new SoftReference<HashMap<String, Bitmap>>(new HashMap<String,Bitmap>());
        return Cache.get();
    }
    */

    public ExecutorService getCacheThreadPoor(){
        if(CacheThreadPoor==null)
            CacheThreadPoor= Executors.newCachedThreadPool();
        return CacheThreadPoor;
    }

    //public BitmapCache getBitmapCache(){ return  bitmapCache;}
}
