package com.mycompany.myapp.Activities.Selection.SelectionAdapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mycompany.myapp.Activities.PlayListSelection;
import com.mycompany.myapp.Activities.Selection.SelectionActivity;
import com.mycompany.myapp.CacheCollection.BaseCache;
import com.mycompany.myapp.CacheCollection.BitmapCache;
import com.mycompany.myapp.CacheCollection.Cache;
import com.mycompany.myapp.CacheCollection.MemoryCache;
//import com.mycompany.myapp.MemoryCache;
import com.mycompany.myapp.ExMusicPlayer;
import com.mycompany.myapp.MusicDataBase;
import com.mycompany.myapp.ParameterSender;
import com.mycompany.myapp.R;
import com.mycompany.myapp.Song;
import com.mycompany.myapp.SongInfoParser;
import com.mycompany.myapp.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by mikir on 2016/7/7.
 */
public class AlbumAdapter extends BaseAdapter {
    ArrayList<Song> arrayList=new ArrayList<>();
    LayoutInflater layoutInflater=null;

    BitmapCache cacheCover=null;
    Context context=null;
    ListView mlistView=null;

    Handler handler=null;
    MusicDataBase musicDataBase=null;

    boolean isPostUpdateRunning=false;

    ExMusicPlayer musicPlayer=null;

    ArrayList<AsyncUpdateTask> asyncUpdateTasks=new ArrayList<>();

    ExecutorService ThreadPoolCache=null;

    public AlbumAdapter(Context ctx, ListView view){
        context=ctx;
        mlistView=view;
        layoutInflater=LayoutInflater.from(context);
        handler=new Handler(Looper.myLooper());
        ThreadPoolCache=Utils.getCacheThreadPool(ctx);
        cacheCover=(BitmapCache)((ParameterSender)ctx.getApplicationContext()).getObject("BitmapCache");
        musicPlayer=(ExMusicPlayer) ((ParameterSender)ctx.getApplicationContext()).getObject("musicplayer");
        musicDataBase=new MusicDataBase(ctx);
    }

    public void addItem(Song song){
        arrayList.add(song);
        notifyDataSetChanged();
    }

    public void removeItem(Song song){
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        RelativeLayout itemView=(RelativeLayout) view;
        if(itemView==null){
            itemView=(RelativeLayout) layoutInflater.inflate(R.layout.item_selection_playlist,null);
        }

        final Song item=(Song)getItem(i);
        itemView.setTag(i);
        ((TextView)itemView.findViewById(R.id.item_selection_playlist_text)).setText(item.Album);
        ((ImageView)itemView.findViewById(R.id.playlist_bg)).setImageBitmap(null);
        ((ImageView)itemView.findViewById(R.id.item_selection_playlist_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new OptionDialog(context,i).show();
            }
        });
        if(cacheCover.Contains(Utils.getBlurCoverName(item.AbsFile_Path))) {
            ((ImageView) itemView.findViewById(R.id.playlist_bg)).setImageBitmap(getCover(item.AbsFile_Path));
        }

        return  itemView;
    }

    public void AsyncSetImageBitmap(int id){
        AsyncUpdateTask ntask=new AsyncUpdateTask();
        ntask.id=id;
        ntask.path=null;
        if(!asyncUpdateTasks.contains(ntask))
            asyncUpdateTasks.add(ntask);

        if(isPostUpdateRunning)
            return;
        isPostUpdateRunning=true;

        ThreadPoolCache.execute(new Runnable() {
            AsyncUpdateTask task=null;
            Bitmap bmp=null;
            @Override
            public void run() {
                while (true) {
                    if (asyncUpdateTasks.size() == 0)
                        break;
                    task = asyncUpdateTasks.remove(0);
                    if (mlistView == null)
                        break;

                    task.path=((Song)getItem(task.id)).AbsFile_Path;
                    bmp = getCover(task.path);
                    task.cover=bmp;

                    handler.post(new UpdateCover(task));

                }
                isPostUpdateRunning=false;
            }
        });
    }

    Bitmap getCover(final String path){
        return cacheCover.get(Utils.getBlurCoverName(path), new BaseCache.OnRequestCache<String, Bitmap>() {
            @Override
            public Bitmap onRequestCache(String s) {
                return Utils.getBlurCover(path);
            }
        });
    }

    class AsyncUpdateTask{
        String path;
        int id;
        Bitmap cover=null;

        @Override
        public boolean equals(Object o) {
            return ((AsyncUpdateTask)o).id==this.id;
        }

        @Override
        public int hashCode() {
            return this.id;
        }
    }

    class UpdateCover implements Runnable{
        AsyncUpdateTask updateTask=null;
        UpdateCover(AsyncUpdateTask task){updateTask=task;}
        @Override
        public void run() {
            RelativeLayout rl = (RelativeLayout)mlistView.findViewWithTag(updateTask.id);
            if (rl == null)
                return;
            ImageView iv = (ImageView) rl.findViewById(R.id.playlist_bg);
            if (iv == null)
                return;
            iv.setImageBitmap(updateTask.cover);
            //Log.i("AsyncUpdateTask(null)", String.format("%d - %s is automatic calling", updateTask.id, updateTask.path));
        }
    }


    public static class SeekUpdater implements ListView.OnScrollListener{
        ListView mListView=null;
        AlbumAdapter albumAdapter=null;
        private int previousFirstVisibleItem = 0;
        private long previousEventTime = 0;
        private double speed = 0;

        private SeekUpdater(){}
        public SeekUpdater(AlbumAdapter albumAdapter1,ListView listView){
            albumAdapter=albumAdapter1;
            mListView=listView;
        }

        public void updateItem(){
            int endPos=mListView.getLastVisiblePosition(),startPos=mListView.getFirstVisiblePosition();
            for(int index=startPos;index<=endPos;index++)
                albumAdapter.AsyncSetImageBitmap(index);
        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (previousFirstVisibleItem != firstVisibleItem){
                    long currTime = System.currentTimeMillis();
                    long timeToScrollOneElement = currTime - previousEventTime;
                    speed = ((double)1/timeToScrollOneElement)*1000;
                    previousFirstVisibleItem = firstVisibleItem;
                    previousEventTime = currTime;
                    if(speed<11){
                        updateItem();
                    }
                    Log.d("ScrollSpeed", "Speed: " +speed + " elements/second");
                }
        }

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
            switch (i){
                case ListView.OnScrollListener.SCROLL_STATE_FLING:
                    break;
                case ListView.OnScrollListener.SCROLL_STATE_IDLE:
                    updateItem();
                    break;
                case ListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    break;
            }
        }
    }

    class OptionDialog extends AlertDialog.Builder{
        final int MSG_ADDTOPLAYPLIST=0;
        final int MSG_UPDATE=1;
        final int MSG_INFO=2;
        final int MSG_DELETE=3;

        Song song=null;
        String item=null;
        int id=-1;

        OptionDialog(Context context,int index){
            super(context);
            id=index;
            song=(Song)getItem(index);
            //=item.name;
            setTitle("专辑列表选项:");
            setItems(new String[]{"播放专辑","检查更新","专辑信息","删除此专辑"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i){
                        case MSG_ADDTOPLAYPLIST:
                            OnAddToPlayList();
                            break;
                        case MSG_INFO:
                            OnInfo();
                            break;
                        case MSG_UPDATE:
                            OnUpdate();
                            break;
                        case MSG_DELETE:
                            OnDelete();
                            break;
                    }
                }
            });
        }

        void OnAddToPlayList(){
            musicPlayer.SwitchAlbum(song.Album);
            ((SelectionActivity)context).finish();
        }

        void OnUpdate(){
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Song[] list=musicDataBase.getSongsFromAlbum(song.Album);
                    int i=0;
                    Song[] newSongs=new Song[list.length];
                    for (Song song : list) {
                        try{
                            Song info = new Song();
                            SongInfoParser parser = new SongInfoParser(song.AbsFile_Path);
                            info.AbsFile_Path = song.AbsFile_Path;
                            info.Artist = parser.getArtist();
                            info.Album = parser.getAlbum();
                            info.Title = parser.getTitle();
                            newSongs[i]=info;
                            i++;
                        }catch (Exception e){
                            Log.i("UpdateDialog",e.getMessage());
                        }
                    }
                    musicDataBase.UpdatePlayList(song.Album,newSongs);
                }
            }.start();
            //dialog.show();
        }

        void OnInfo(){
            //// TODO: 2016/7/7
        }
        void OnDelete(){
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            builder.setTitle("提示");
            builder.setMessage(String.format("你确定要删除这个专辑\"%s\"吗?(并不会删除源文件)",song.Album));
            builder.setNegativeButton("取消",null);
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    musicDataBase.DeleteAlbum(song.Album);
                    removeItem((Song)getItem(id));
                }
            });
        }
    }

}
