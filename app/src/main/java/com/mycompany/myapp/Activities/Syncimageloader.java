package com.mycompany.myapp.Activities;

import android.graphics.Bitmap;
import android.os.Handler;

import com.mycompany.myapp.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mikir on 2016/7/4.
 */
public class Syncimageloader {
    private Object lock = new Object();

    boolean isRunning=false;

    private boolean mAllowLoad = true;

    private boolean firstLoad = true;

    private int mStartLoadLimit = 0;

    private int mStopLoadLimit = 0;

    ArrayList<PlayListSelectionListAdapter.AysncUpdateCoverTask> aysncUpdateCoverTaskArrayList=new ArrayList<>();

    final Handler handler = new Handler();

    HashMap<String,Bitmap> imagecache=new HashMap<>();

    OnImageLoadListener onImageLoadListener=null;

    public interface OnImageLoadListener {
        public void onImageLoad(Integer t, Bitmap drawable);
        public void onError(Integer t);
    }

    public void addTask(PlayListSelectionListAdapter.AysncUpdateCoverTask task){
        aysncUpdateCoverTaskArrayList.add(task);
    }

    public void setOnImageLoadListener(OnImageLoadListener loadListener){onImageLoadListener=loadListener;}


    public void lock(){
        mAllowLoad = false;
        firstLoad = false;
    }

    public void unlock(){
        mAllowLoad = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void startUpdate() {
        if(isRunning)
            return;
        isRunning=true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(aysncUpdateCoverTaskArrayList.size()!=0){
                    PlayListSelectionListAdapter.AysncUpdateCoverTask task= aysncUpdateCoverTaskArrayList.remove(0);
                    loadImage(task.path,task._id);
                }
                isRunning=false;
            }

        }).start();
    }

    private void loadImage(final String mImageUrl,final Integer mt){
        try {
            final Bitmap d = loadImage(mImageUrl);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(mAllowLoad){
                        onImageLoadListener.onImageLoad(mt, d);
                    }
                }
            });
        } catch (IOException e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onImageLoadListener.onError(mt);
                }
            });
            e.printStackTrace();
        }
    }

    public Bitmap loadImage(String path) throws IOException {
        return Utils.getCover(path);
    }
}
