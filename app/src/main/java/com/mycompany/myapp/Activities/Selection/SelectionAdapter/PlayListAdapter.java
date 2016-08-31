package com.mycompany.myapp.Activities.Selection.SelectionAdapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.TextView;
import android.widget.Toast;

import com.mycompany.myapp.Activities.PlayListSelection;
import com.mycompany.myapp.Activities.Selection.SelectionActivity;
import com.mycompany.myapp.CacheCollection.BaseCache;
import com.mycompany.myapp.CacheCollection.BitmapCache;
import com.mycompany.myapp.CacheCollection.Cache;
import com.mycompany.myapp.CacheCollection.MemoryCache;
import com.mycompany.myapp.ExMusicPlayer;
//import com.mycompany.myapp.MemoryCache;
import com.mycompany.myapp.MusicDataBase;
import com.mycompany.myapp.ParameterSender;
import com.mycompany.myapp.R;
import com.mycompany.myapp.Song;
import com.mycompany.myapp.SongInfoParser;
import com.mycompany.myapp.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Created by mikir on 2016/7/6.
 */
public class PlayListAdapter extends BaseAdapter{

    ArrayList<PlayListItem> playListItems=new ArrayList<>();
    LayoutInflater layoutInflater=null;
    //HashMap<String,Bitmap> cacheBitmap=new HashMap<>();
    Context context=null;
    MusicDataBase dataBase=null;

    ExMusicPlayer player=null;

    Handler handler=null;

    ListView mListView=null;
    boolean isPostUpdateRunning=false;

    ArrayList<AsyncUpdateTask> asyncUpdateTasks=new ArrayList<>();
    BitmapCache bitmapCache=null;
    ExecutorService ThreadPoolCache=null;

    //ArrayList<AsyncUpdateTask> asyncUpdateTasks=new ArrayList<>();
    ArrayList<PlayListItem> asyncAddCacheTasks=new ArrayList<>();

    private PlayListAdapter(){}
    public PlayListAdapter(Context ctx,ListView listView,ExMusicPlayer player){
        context=ctx;
        layoutInflater=LayoutInflater.from(ctx);
        handler=new Handler(Looper.myLooper());
        mListView=listView;
        dataBase=new MusicDataBase(ctx);
        this.player=player;
        ThreadPoolCache=Utils.getCacheThreadPool(context);
        //cacheBitmap=((ParameterSender)ctx.getApplicationContext()).getCacheBitmapMap();
        bitmapCache=(BitmapCache)((ParameterSender)context.getApplicationContext()).getObject("BitmapCache");
    }

    public void addItem(PlayListItem item){
        addItem(item,playListItems.size());
    }

    public void addItem(PlayListItem item,int pos){
        if(item.firstSong==null)
            return;
        playListItems.add(pos,item);
        notifyDataSetChanged();
    }

    void removeItem(int id){
        playListItems.remove(id);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return playListItems.size();
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Object getItem(int i) {
        return playListItems.get(i);
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        RelativeLayout itemView=(RelativeLayout) view;
        if(itemView==null){
            itemView=(RelativeLayout) layoutInflater.inflate(R.layout.item_selection_playlist,null);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent();
                    intent.setClass(context, PlayListSelection.class);
                    PlayListItem item=((PlayListItem)view.getTag(R.id.item_save));
                    Log.d("MainActivity",String.format("now put %s into intent",item.name));
                    intent.putExtra("playlist_name",item.name);
                    context.startActivity(intent);
                }
            });
            ((ImageButton)itemView.findViewById(R.id.item_selection_playlist_button)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PlayListOptionDialog dialog=new PlayListOptionDialog(context,i);
                    dialog.show();
                }
            });
        }

        final PlayListItem item=(PlayListItem)getItem(i);
        itemView.setTag(i);
        itemView.setTag(R.id.item_save,item);
        ((TextView)itemView.findViewById(R.id.item_selection_playlist_text)).setText(item.name);
        //((ImageView)itemView.findViewById(R.id.playlist_bg)).setImageBitmap(bitmapCache.get());
        if(bitmapCache.Contains(Utils.getBlurCoverName(item.firstSong.AbsFile_Path))) {
            ((ImageView) itemView.findViewById(R.id.playlist_bg)).setImageBitmap(getCover(item.firstSong.AbsFile_Path));
        }
        return  itemView;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
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

                    task.path=((PlayListItem)getItem(task.id)).firstSong.AbsFile_Path;
                    bmp = getCover(task.path);
                    task.cover=bmp;

                    handler.post(new UpdateCover(task));
                }
                isPostUpdateRunning=false;
            }
        });
    }

    Bitmap getCover(final String path){
        return bitmapCache.get(Utils.getBlurCoverName(path), new BaseCache.OnRequestCache<String, Bitmap>() {
            @Override
            public Bitmap onRequestCache(String s) {
                return Utils.getBlurCover(path);
            }
        });
    }

    public static class PlayListItem{
        public String name;
        public Song firstSong=null;
    }

    class UpdateCover implements Runnable{
        AsyncUpdateTask updateTask=null;
        UpdateCover(AsyncUpdateTask task){updateTask=task;}
        @Override
        public void run() {
            RelativeLayout rl = (RelativeLayout)mListView.findViewWithTag(updateTask.id);
            if (rl == null)
                return;
            ImageView iv = (ImageView) rl.findViewById(R.id.playlist_bg);
            if (iv == null)
                return;
            iv.setImageBitmap(updateTask.cover);
            //Log.i("AsyncUpdateTask(null)", String.format("%d - %s is automatic calling", updateTask.id, updateTask.path));
        }
    }

    class AsyncUpdateTask{
        String path;
        int id;
        Bitmap cover;
    }

    class PlayListOptionDialog extends AlertDialog.Builder{
        final int MSG_ADDTOPLAYPLIST=0;
        final int MSG_UPDATE=1;
        final int MSG_RENAME=2;
        final int MSG_SETCOVER=3;
        final int MSG_INFO=4;
        final int MSG_DELETE=5;

        String playlist=null;
        PlayListItem item=null;
        int id=-1;

        PlayListOptionDialog(Context context,int index){
            super(context);
            id=index;
            item=playListItems.get(index);
            playlist=item.name;
            setTitle("歌曲列表选项:");
            setItems(new String[]{"播放列表","检查更新","重命名","设置封面","列表信息","删除此列表"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i){
                        case MSG_UPDATE:
                            OnUpdate();
                            break;
                        case MSG_RENAME:
                            OnRename();
                            break;
                        case MSG_SETCOVER:
                            OnSetCover();
                            break;
                        case MSG_INFO:
                            OnInfo();
                            break;
                        case MSG_DELETE:
                            OnDelete();
                            break;
                        case MSG_ADDTOPLAYPLIST:
                            OnAddToPlayList();
                            break;
                    }
                }
            });
        }

        void OnAddToPlayList(){
            player.SwitchPlayList(playlist);
            ((SelectionActivity)context).finish();
        }

        void OnUpdate(){
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Song[] list=dataBase.LoadPlayList(item.name);
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
                    dataBase.UpdatePlayList(playlist,newSongs);
                }
            }.start();
            //dialog.show();
        }
        void OnRename(){
            AlertDialog.Builder dialog=new AlertDialog.Builder(context);
            dialog.setTitle("重命名播放列表");
            final EditText textView=new EditText(context);
            textView.setHint("请输入新的名字");
            dialog.setView(textView);
            dialog.setNegativeButton("取消",null);
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final String newName=textView.getText().toString();
                    ThreadPoolCache.execute(new Runnable() {
                        @Override
                        public void run() {
                            if(Looper.myLooper()==null)
                                Looper.prepare();
                            Song[] songs=dataBase.LoadPlayList(playlist);
                            dataBase.SavePlayList(newName,songs);
                            dataBase.DeletePlayList(playlist);
                            Toast.makeText(context,"重命名完成",Toast.LENGTH_SHORT).show();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    removeItem(id);
                                    PlayListItem listItem=new PlayListItem();
                                    listItem.name=newName;
                                    listItem.firstSong=(dataBase.getFirstSongOfPlayList(newName));
                                    addItem(listItem,id);
                                }
                            });
                        }
                    });
                }
            });
            dialog.show();
        }
        void OnSetCover(){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final EditText editText = new EditText(context);
            editText.setHint("input new cover picture path.");
            builder.setMessage("Set cover picture path");
            builder.setView(editText);
            builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String name = editText.getText().toString();
                    if (name.length() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("图片路径不能为空!");
                        builder.setPositiveButton("了解", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                    } else{
                        try {
                            File coverFile = new File(name);
                            FileInputStream inputStream = new FileInputStream(coverFile);
                            byte[] buffer=new byte[inputStream.available()];
                            inputStream.read(buffer);
                            Cache.saveCacheData(playlist + "_cover",buffer);
                        }catch (Exception e){

                        }
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
        }
        void OnInfo(){
            //// TODO: 2016/7/7
        }
        void OnDelete(){
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            builder.setTitle("提示");
            builder.setMessage(String.format("确定要删除歌单\"%s\"吗?",playlist));
            builder.setNegativeButton("取消",null);
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dataBase.DeletePlayList(playlist);
                    removeItem(id);
                }
            });
        }

        class CallerDialogUpdate implements Runnable {
            int i = 0, size = 0;
            ProgressDialog dialog;
            Song curSong = null;
            @Override
            public void run() {
                i++;
                dialog.setProgressNumberFormat(String.format("%1d/%2d", i, size));
                dialog.setProgress((int) (i * 1.0f / size * 100.0f));
                dialog.setMessage(String.format("%s - %s", curSong.Artist, curSong.Title));
            }
        }
    }

    public static class SeekUpdater implements ListView.OnScrollListener{
        ListView mListView=null;
        PlayListAdapter albumAdapter=null;
        private int previousFirstVisibleItem = 0;
        private long previousEventTime = 0;
        private double speed = 0;

        private SeekUpdater(){}
        public SeekUpdater(PlayListAdapter albumAdapter1,ListView listView){
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
}
