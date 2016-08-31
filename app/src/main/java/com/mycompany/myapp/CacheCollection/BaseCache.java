package com.mycompany.myapp.CacheCollection;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.SoftReference;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by mikir on 2016/7/16.
 */
/*
public abstract class BaseCache<KEY,VALUE> {
        HashMap<KEY,Long> timeRecorder=new HashMap<>();

        TreeMap<KEY,VALUE> cacheMap=new TreeMap<>(new Comparator<KEY>() {
            @Override
            public int compare(KEY key, KEY t1) {
                if(key.equals(t1))
                    return 0;

                long inv=(getRecordTime(t1)-getRecordTime(key));
                Log.i("BaseCache",String.format("%s(%d) - %s(%d) (%d)",key,getRecordTime(key),t1,getRecordTime(t1),inv));
                return inv>0?1:-1;
            }
        });

    long cacheMaxSize=150000000;//150m size
    long curSize=0;

    public BaseCache(){Init();}

    public void Init(){
    }


    long getRecordTime(KEY key){
        if(timeRecorder.containsKey(key))
            return timeRecorder.get(key);
        return getTimeNow();
    }

    String getOutputMessage(){return ((VALUE)(null)).getClass().getName();}

    void saveRecordTime(KEY key,long time){timeRecorder.put(key,time);}


    //public void put(String key,Object cacheObject)throws MemoryCacheException{put(key,cacheObject);}
    public void put(KEY key,VALUE cacheObject)throws Exception{
        Log.i("BaseCache",String.format("Save %s CacheObject",key.toString()));
        long size=getSize(cacheObject);
        while(curSize+size>cacheMaxSize)
        {
            Log.i("BaseCache",String.format("call GC ! %d / %d",curSize,cacheMaxSize));
            collectGC();
        }

        curSize+=size;

        cacheMap.put(key,cacheObject);
    }




    private long getTimeNow(){return System.currentTimeMillis();}



    private VALUE requestCacheObject(KEY key,OnRequestCache<KEY,VALUE> onRequestCache){
        VALUE object=onRequestCache.onRequestCache(key);
        try {
            put(key, object);
        }catch (Exception e){
            Log.i("BaseCache",String.format("Getting %s CacheObject from Requesting Callback was failed because %s",key,e.getMessage()));
            return null;
        }
        Log.i("BaseCache",String.format("Get %s CacheObject from Requesting Callback",key));
        return object;
    }

    public VALUE get(KEY key,OnRequestCache<KEY,VALUE> failedRequest){
        Log.i("BaseCache","-----------------------------------------");
        if(!cacheMap.containsKey(key)){
            Log.i("BaseCache",key.toString()+" is not found in cacheMap!");
            return requestCacheObject(key,failedRequest);
        }
        VALUE cacheData=cacheMap.get(key);
        if(cacheData==null){
            Log.i("BaseCache",String.format("the value of %s is null",key.toString()));
            return requestCacheObject(key,failedRequest);
        }
        saveRecordTime(key,getTimeNow());
        Log.i("BaseCache",String.format("Get %s CacheObject from Cache",key.toString()));
        return cacheData;
    }

    public void collectGC(){
        Map.Entry<KEY,VALUE> cacheDataEntry=cacheMap.firstEntry();
        cacheMap.remove(cacheDataEntry.getKey());
        curSize-=getSize(cacheDataEntry.getValue());
        Log.i("BaseCache",String.format("%s is removed by GC and release %dkb",cacheDataEntry.getKey().toString(),getSize(cacheDataEntry.getValue())));
        return;
    }

    public static interface OnRequestCache<KEY,VALUE>{
        VALUE onRequestCache(KEY key);
    }

    public abstract long getSize(VALUE value);



    public boolean Contains(KEY key){
        if(!cacheMap.containsKey(key))
            return false;
        else if(cacheMap.get(key)==null)
            return false;
        return true;
    }

}
*/


public abstract class BaseCache<KEY,VALUE> {
    ArrayList<KEY> recordHistory=new ArrayList<KEY>();

    HashMap<KEY,VALUE> cacheMap=new HashMap<>();

    long cacheMaxSize=150000000;//150m size
    long curSize=0;

    public BaseCache(){Init();}

    public void Init(){
    }

    String getOutputMessage(){return ((VALUE)(null)).getClass().getName();}

    //public void put(String key,Object cacheObject)throws MemoryCacheException{put(key,cacheObject);}
    public void put(KEY key,VALUE cacheObject)throws Exception{
        if(cacheObject==null)
            return;
        long size=getSize(cacheObject);
        while(curSize+size>cacheMaxSize)
        {
            collectGC();
        }
        updateHistory(key);
        curSize+=size;
        cacheMap.put(key,cacheObject);
    }

    private void updateHistory(KEY key){
        if(recordHistory.contains(key)){
            deleteHistory(key);
        }
        recordHistory.add(key);
    }

    private KEY getLastHistory(){
        return (recordHistory.size()==0)?null:recordHistory.get(0);
    }

    private void deleteHistory(KEY key){
        recordHistory.remove(key);
    }

    private VALUE requestCacheObject(KEY key,OnRequestCache<KEY,VALUE> onRequestCache){
        if(onRequestCache==null)
            return null;
        VALUE object=onRequestCache.onRequestCache(key);
        try {
            put(key, object);
        }catch (Exception e){
            return null;
        }
        return object;
    }

    public VALUE get(KEY key,OnRequestCache<KEY,VALUE> failedRequest){
        if(!cacheMap.containsKey(key)){
            return requestCacheObject(key,failedRequest);
        }
        VALUE cacheData=cacheMap.get(key);
        if(cacheData==null){
            return requestCacheObject(key,failedRequest);
        }
        updateHistory(key);
        return cacheData;
    }

    public void collectGC(){
        KEY key=getLastHistory();
        if(key==null)
            return;
        deleteHistory(key);
        VALUE value=cacheMap.remove(key);
        curSize-=getSize(value);
        Log.i("BaseCache",String.format("%s is removed by GC and release %dkb",key.toString(),getSize(value)));
        return;
    }

    public void force_collectGC(){
        cacheMap.clear();
    }

    public static interface OnRequestCache<KEY,VALUE>{
        VALUE onRequestCache(KEY key);
    }

    public abstract long getSize(VALUE value);



    public boolean Contains(KEY key){
        if(!cacheMap.containsKey(key))
            return false;
        else if(cacheMap.get(key)==null)
            return false;
        return true;
    }

}
