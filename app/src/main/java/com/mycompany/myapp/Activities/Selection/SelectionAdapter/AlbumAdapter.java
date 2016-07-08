package com.mycompany.myapp.Activities.Selection.SelectionAdapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mycompany.myapp.Activities.PlayListSelection;
import com.mycompany.myapp.R;
import com.mycompany.myapp.Song;
import com.mycompany.myapp.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mikir on 2016/7/7.
 */
public class AlbumAdapter extends BaseAdapter {
    ArrayList<Song> arrayList=new ArrayList<>();
    LayoutInflater layoutInflater=null;

    HashMap<String,Drawable> cacheCover=new HashMap<>();
    Context context=null;
    ListView mlistView=null;

    Handler handler=null;

    boolean isPostUpdateRunning=false;

    ArrayList<AsyncUpdateTask> asyncUpdateTasks=new ArrayList<>();

    public AlbumAdapter(Context ctx, ListView view){
        context=ctx;
        mlistView=view;
        layoutInflater=LayoutInflater.from(context);
        handler=new Handler(Looper.myLooper());
    }

    void addItem(Song song){
        arrayList.add(song);
        notifyDataSetChanged();
    }

    void removeItem(Song song){
        arrayList.remove(song);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        RelativeLayout itemView=(RelativeLayout) view;
        if(itemView==null){
            itemView=(RelativeLayout) layoutInflater.inflate(R.layout.item_selection_playlist,null);
        }

        Song item=(Song) getItem(i);
        itemView.setTag(i);
        itemView.setTag(5,item);
        ((TextView)itemView.findViewById(R.id.item_selection_playlist_text)).setText(item.Album);
        AsyncSetImageBitmap(i,item.AbsFile_Path);
        return  itemView;
    }

    void AsyncSetImageBitmap(int id,String path){
        AsyncUpdateTask ntask=new AsyncUpdateTask();
        ntask.id=id;
        ntask.path=path;
        asyncUpdateTasks.add(ntask);

        if(isPostUpdateRunning)
            return;
        isPostUpdateRunning=true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if(asyncUpdateTasks.size()==0)
                                break;
                            AsyncUpdateTask task = asyncUpdateTasks.remove(0);
                            if (mlistView == null)
                                return;
                            RelativeLayout rl = (RelativeLayout) mlistView.findViewWithTag(task.id);
                            if (rl == null)
                                return;
                            rl.setBackground(getCover(task.path));
                            Log.i("AsyncUpdateTask", String.format("%d - %s is automatic calling", task.id, task.path));
                        }
                    }
                });
                isPostUpdateRunning=false;
            }
        }).start();
    }

    Drawable getCover(String path){
        if(!cacheCover.containsKey(path)){
            Drawable bmp=Drawable.createFromStream(Utils.getCoverStream(path),"nulllocal");
            cacheCover.put(path,(bmp));
            return bmp;
        }
        return cacheCover.get(path);
    }

    class AsyncUpdateTask{
        String path;
        int id;
    }

}
