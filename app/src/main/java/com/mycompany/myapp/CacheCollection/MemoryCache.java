package com.mycompany.myapp.CacheCollection;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mikir on 2016/7/12.
 */
public final class MemoryCache{

    private static ConcurrentHashMap<String,CacheData> Cache=new ConcurrentHashMap<>();

    private static Context context=null;
    private static long timeLimit=-1;
    private static boolean ableOverwrite=true;
    private static MaintenanceThread maintenanceThread;
    private static boolean ableCache=true;

    private MemoryCache(){}

    public static void Init(Context ctx){
        //byte[] data=new byte[100000000];
        context=ctx;
        maintenanceThread=new MaintenanceThread();
        //maintenanceThread.start();
    }

    public static void setCacheTimeLimit(long time){timeLimit=time;}
    public static void setAbleCache(boolean b){ableCache=b;}
    public static boolean getAbleCache(){return ableCache;}
    public static void setAbleOverWrite(boolean ableOverWrite){ableOverwrite=ableOverWrite;}






    public static void put(String key,Object cacheObject)throws MemoryCacheException{put(key,cacheObject,-1);}
    public static void put(String key,Object cacheObject,long timelimit)throws MemoryCacheException{
        if(!ableCache)
            return;
        if(Cache.containsKey(key))
            if (!ableOverwrite)
                throw new MemoryCacheException("Cant overwrite this key until you set ableOverWrite true");
        CacheData cacheData=new CacheData();
        cacheData.referenceObject=new SoftReference<Object>(cacheObject);
        cacheData.limitTime=timelimit<=0?timelimit:timeLimit;
        cacheData.recordTime=getTimeNow();

        Log.i("MemoryCache",String.format("Save %s CacheObject",key));
        Cache.put(key,cacheData);
    }




    private static long getTimeNow(){return System.currentTimeMillis();}



    private static Object requestCacheObject(String key,OnRequestCache onRequestCache){
        Object object=onRequestCache.onRequestCache(key);
        try {
            put(key, object);
        }catch (Exception e){

        }
        Log.i("MemoryCache",String.format("Get %s CacheObject from Requesting Callback",key));
        return object;
    }

    public static Object get(String key,OnRequestCache failedRequest){
        return get(key,failedRequest,-1);
    }

    public static Object get(String key,OnRequestCache failedRequest,long timeLimit){
        if(!ableCache)
            return requestCacheObject(key,failedRequest);
        CacheData cacheData;
        if(!Cache.containsKey(key)){
            return requestCacheObject(key,failedRequest);
        }
        cacheData=Cache.get(key);
        if(cacheData.referenceObject==null){
            return requestCacheObject(key,failedRequest);
        }
        if(cacheData.referenceObject.get()==null){
            return requestCacheObject(key,failedRequest);
        }

        if(cacheData.limitTime>0)
            if((cacheData.limitTime+cacheData.recordTime)>getTimeNow())
                return requestCacheObject(key,failedRequest);
        cacheData.recordTime=getTimeNow();
        Log.i("MemoryCache",String.format("Get %s CacheObject from Cache",key));
        return cacheData.referenceObject.get();
    }

    public static void collectGC(){
        new MaintenanceThread().start();
        return;
    }

    public interface OnRequestCache{
        Object onRequestCache(String key);
    }

    public static class MaintenanceThread extends Thread{

        public MaintenanceThread(){
            super();
        }

        @Override
        public void run() {
               this.Check();
        }

        private void Check(){
            CacheData cacheData = null;
            for (Map.Entry<String, CacheData> entry : Cache.entrySet()) {
                cacheData = entry.getValue();
                if (cacheData.referenceObject == null || ((cacheData.limitTime + cacheData.recordTime) > getTimeNow())||cacheData.referenceObject.get()==null) {
                    cacheData.referenceObject = null;
                    Cache.remove(entry.getKey());
                    Log.i("MemoryCache",String.format("Remove CacheObject %s by GC(%d+%d=%d > %d)",entry.getKey(),cacheData.recordTime,cacheData.limitTime,cacheData.limitTime+cacheData.recordTime,getTimeNow()));
                }
            }
        }
    }

    private static class CacheData{
        long limitTime=-1;
        SoftReference<Object> referenceObject=null;
        long recordTime=-1;
    }

    public static class MemoryCacheException extends Exception{
        String Message=null;
        MemoryCacheException(String msg){
            super();
        }

        @Override
        public String getMessage() {
            return Message;
        }
    }

    public static boolean Contains(String key){return ableCache&&(Cache.containsKey(key)?Cache.get(key).referenceObject.get()!=null:false);}

    @Override
    protected void finalize() throws Throwable {
        maintenanceThread.stop();
        super.finalize();
    }
}
