package com.mycompany.myapp.Activities;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.*;
import android.content.*;

import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ExecutorService;

import android.view.*;
import android.graphics.*;
import android.util.*;

import com.mycompany.myapp.CacheCollection.BaseCache;
import com.mycompany.myapp.CacheCollection.BitmapCache;
import com.mycompany.myapp.CacheCollection.MemoryCache;
import com.mycompany.myapp.ExMusicPlayer;
//import com.mycompany.myapp.MemoryCache;
import com.mycompany.myapp.ParameterSender;
import com.mycompany.myapp.R;
import com.mycompany.myapp.Song;
import com.mycompany.myapp.Utils;

public class PlayListSelectionListAdapter extends BaseAdapter
{
    ListView mListView=null;
    int head,item;
    Context ctx=null;
    LayoutInflater inflater=null;
    ArrayList<Song> item_list=null;
    PlayListInfo headInfo=null;
    //Handler handler;
    BitmapCache bitmapCache=null;

    ExMusicPlayer musicPlayer=null;

    Handler handler=null;
    ArrayList<AsyncUpdateTask> asyncUpdateTasks=new ArrayList<>();

    ExecutorService ThreadPoolCache=null;

    public final int Type_Head=0;
    public final int Type_Item=1;

    boolean isPostUpdateRunning=false;

    private PlayListSelectionListAdapter()
    {}
    public PlayListSelectionListAdapter(Context _ctx, LayoutInflater _inflater)
    {
        Init(_ctx);
        inflater = _inflater;
    }

    public void SetHead(PlayListInfo fd)
    {
        headInfo=fd;
    }


    public PlayListSelectionListAdapter(Context _ctx)
    {
        Init(_ctx);
    }

    void Init(Context _ctx){
        ctx = _ctx;
        inflater = LayoutInflater.from(_ctx);
        head = R.layout.item_head_playlistselection;
        item = R.layout.item_playlistselection;
        item_list = new ArrayList<Song>();
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
        ThreadPoolCache=Utils.getCacheThreadPool(ctx);
        bitmapCache=(BitmapCache)((ParameterSender)ctx.getApplicationContext()).getObject("BitmapCache");
    }

    void setMusicPlayer(ExMusicPlayer player){
        musicPlayer=player;
    }

    void addItem(Song sd)
    {
        item_list.add(sd);
    }

    void setListView(ListView listView){
        mListView=listView;
        /*
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("PlayNowClickListener","click");
                if(i==0)
                    return;
                Song sd=(Song)getItem(i);
                String curPlaylistName=headInfo.Name;
                int pos=item_list.indexOf(sd.AbsFile_Path);
                if(musicPlayer.getCurrentPlayListName()!=curPlaylistName){
                    musicPlayer.SwitchPlayList(curPlaylistName);
                }
                musicPlayer.Play(pos);
            }
        });*/
    }

    @Override
    public Object getItem(int p1)
    {
        return p1==0?headInfo:item_list.get(p1-1);
    }

    @Override
    public int getItemViewType(int position)
    {
        return (position == 0) ?Type_Head: Type_Item;
    }

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public View getView(final int p1, View p2, ViewGroup p3)
    {
        RelativeLayout rl=null;
        Song data=null;
        if (p2 == null)
        {
            switch (getItemViewType(p1))
            {
                case Type_Head:
                    rl = (RelativeLayout)inflater.inflate(head, null);
                    break;
                case Type_Item:
                    rl = (RelativeLayout)inflater.inflate(item, null);
                    break;
            }

        }
        else
        {
            rl = (RelativeLayout)p2;
        }
        rl.setTag(p1);
        try
        {
            Bitmap bmp=null;
            PlayListInfo fd=null;
            ImageView imageView=null;
            TextView textView=null;
            //rl.setTag(p1);

            switch (getItemViewType(p1))
            {
                case Type_Head:
                    fd = headInfo;
                    imageView=(ImageView)rl.findViewById(R.id.folder_bg);
                    ((TextView)rl.findViewById(R.id.folder_name)).setText(headInfo.Name);
                    ((TextView)rl.findViewById(R.id.folder_owner)).setText(headInfo.Owner);
                    if (headInfo.CoverImage==null)
                    {
                        if(bitmapCache.Contains(Utils.getCoverName(item_list.get(0).AbsFile_Path))) {
                            ((ImageView)rl.findViewById(R.id.folder_bg)).setImageBitmap(getCover(item_list.get(0).AbsFile_Path));
                        }
                    }
                    else
                    {
                        imageView.setImageBitmap(fd.CoverImage);
                    }
                    break;
                case Type_Item:
                    final Song sd = (Song) item_list.get((int)getItemId(p1));
                    ((TextView)rl.findViewById(R.id.song_artist)).setText(sd.Artist);
                    ((TextView)rl.findViewById(R.id.song_tittle)).setText(sd.Title);
                    ((ImageButton)rl.findViewById(R.id.play_button)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.i("PlayNowClickListener", "click");
                            String curPlaylistName = headInfo.Name;
                            if (musicPlayer.getCurrentPlayListName() != curPlaylistName) {
                                musicPlayer.SwitchPlayList(curPlaylistName);
                            }
                            musicPlayer.Play(p1-1);
                        }
                    });
                    if(bitmapCache.Contains(Utils.getCoverName(sd.AbsFile_Path))) {
                        ((ImageView)rl.findViewById(R.id.song_cover)).setImageBitmap(getCover(sd.AbsFile_Path));
                    }
            }
            return rl;
        }
        catch (Exception e)
        {
            e.fillInStackTrace();
            Log.d("GetView", String.format("Pos : %d is failed casue %s", p1, e.getMessage()));

        }
        return rl;
    }

    @Override
    public long getItemId(int p1)
    {
        return p1==0?0:p1-1;
    }

    @Override
    public int getCount()
    {
        return item_list.size();
    }

    public void Dump()
    {
        Song sd=null;
        for (int i=1;i < item_list.size();i++)
        {
            sd = (Song) item_list.get(i);
            Log.d("Dump", String.format("%d : %s - %s", i, sd.Artist, sd.Title));
        }
    }

    String getPlayListName(){
        return "";
    }

    String getPlayListCount(){
        return "";
    }

    Bitmap getCover(String path){
        return bitmapCache.get(Utils.getCoverName(path), new BaseCache.OnRequestCache<String, Bitmap>() {
            @Override
            public Bitmap onRequestCache(String s) {
                return Utils.getCover(s);
            }
        });
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
                    if (mListView == null)
                        break;

                    task.path=(item_list.get((int)getItemId(task.id))).AbsFile_Path;
                    bmp = getCover(task.path);
                    task.cover=bmp;
                    handler.post(new UpdateCover(task));
                    //Log.i("AsyncSetImageBitmap",String.format("%s - %s (%s)",task.cover.toString(),task.id,mListView.toString()));
                }
                isPostUpdateRunning=false;
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

    public static class PlayListInfo{
        public String Name,Owner;
        public Bitmap CoverImage=null;
    }

    class AysncUpdateCoverTask{
        int _id;
        String path;
    }

    class UpdateCover implements Runnable{
        AsyncUpdateTask updateTask=null;
        UpdateCover(AsyncUpdateTask task){updateTask=task;}
        @Override
        public void run() {
            RelativeLayout rl = (RelativeLayout)mListView.findViewWithTag(updateTask.id);
            if (rl == null)
                return;
            if(updateTask.id==0) {
                ImageView imageView=(ImageView)rl.findViewById(R.id.folder_bg);
                if (headInfo.CoverImage == null) {
                    if (bitmapCache.Contains(Utils.getCoverName(item_list.get(0).AbsFile_Path))) {
                        imageView.setImageBitmap(getCover(item_list.get(0).AbsFile_Path));
                    }
                } else {
                    imageView.setImageBitmap(headInfo.CoverImage);
                }
                return;
            }
            ImageView iv = (ImageView)rl.findViewById(R.id.song_cover);
            if (iv == null)
                return;
            iv.setImageBitmap(updateTask.cover);
            //Log.i("AsyncUpdateTask(null)", String.format("%d - %s is automatic calling", updateTask.id, updateTask.path));
        }
    }

    public static class SeekUpdater implements ListView.OnScrollListener{
        ListView mListView=null;
        PlayListSelectionListAdapter albumAdapter=null;
        private int previousFirstVisibleItem = 0;
        private long previousEventTime = 0;
        private double speed = 0;

        private SeekUpdater(){}
        public SeekUpdater(PlayListSelectionListAdapter albumAdapter1,ListView listView){
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
                    Log.d("ScrollSpeed", "call");
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
/*
    class PlayNowClickListener implements RelativeLayout.OnClickListener{
        @Override
        public void onClick(View view) {
            Log.i("PlayNowClickListener","click");
            Song song=(Song)view.getTag();
            String curPlaylistName=headInfo.Name;
            int pos=item_list.indexOf(song.AbsFile_Path);
            if(musicPlayer.getCurrentPlayListName()!=curPlaylistName){
                musicPlayer.SwitchPlayList(curPlaylistName);
            }
            musicPlayer.Play(pos);
        }
    }*/

}