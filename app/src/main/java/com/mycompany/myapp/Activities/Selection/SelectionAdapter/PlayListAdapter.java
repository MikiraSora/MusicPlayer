package com.mycompany.myapp.Activities.Selection.SelectionAdapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mycompany.myapp.Activities.PlayListSelection;
import com.mycompany.myapp.Activities.Selection.SelectionActivity;
import com.mycompany.myapp.Cache;
import com.mycompany.myapp.ExMusicPlayer;
import com.mycompany.myapp.MusicDataBase;
import com.mycompany.myapp.R;
import com.mycompany.myapp.Song;
import com.mycompany.myapp.SongInfoParser;
import com.mycompany.myapp.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.OptionalDataException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mikir on 2016/7/6.
 */
public class PlayListAdapter extends BaseAdapter{

    ArrayList<PlayListItem> playListItems=new ArrayList<>();
    LayoutInflater layoutInflater=null;
    HashMap<String,Drawable> cacheBitmap=new HashMap<>();
    Context context=null;
    MusicDataBase dataBase=null;

    ExMusicPlayer player=null;

    Handler handler=null;

    ListView mListView=null;

    boolean isUpdateRunning=false;
    boolean isPostUpdateRunning=false;

    ArrayList<AsyncUpdateTask> asyncUpdateTasks=new ArrayList<>();
    ArrayList<PlayListItem> asyncAddCacheTasks=new ArrayList<>();

    private PlayListAdapter(){}
    public PlayListAdapter(Context ctx,ListView listView,ExMusicPlayer player){
        context=ctx;
        layoutInflater=LayoutInflater.from(ctx);
        handler=new Handler(Looper.myLooper());
        mListView=listView;
        dataBase=new MusicDataBase(ctx);
        this.player=player;
    }

    public void addItem(PlayListItem item){
        if(item.firstSong==null)
            return;
        playListItems.add(item);
        asyncAddCacheTasks.add(item);

        if(isUpdateRunning)
            return;
        else {
            isUpdateRunning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (asyncAddCacheTasks.size() != 0) {
                            final PlayListItem task = asyncAddCacheTasks.remove(0);
                            Log.i("PlayListAdapter",String.format("task : name=%s , firstsong=%s",task.name,task.firstSong));
                            if (!cacheBitmap.containsKey(task.firstSong.AbsFile_Path)) {
                                cacheBitmap.put(task.firstSong.AbsFile_Path,getCover(task.firstSong.AbsFile_Path));
                            }
                        } else {
                            break;
                        }
                    }
                    isUpdateRunning = false;
                }
            }).start();
        }
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

        PlayListItem item=(PlayListItem)getItem(i);
        itemView.setTag(i);
        itemView.setTag(R.id.item_save,item);
        ((TextView)itemView.findViewById(R.id.item_selection_playlist_text)).setText(item.name);
        AsyncSetImageBitmap(i,item.firstSong.AbsFile_Path);
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
                            if (mListView == null)
                                return;
                            RelativeLayout rl = (RelativeLayout) mListView.findViewWithTag(task.id);
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
        if(!cacheBitmap.containsKey(path)){
            Drawable bmp=Drawable.createFromStream(Utils.getCoverStream(path),"nulllocal");
            cacheBitmap.put(path,(bmp));
            return bmp;
        }
        return cacheBitmap.get(path);
    }

    public static class PlayListItem{
        public String name;
        public Song firstSong=null;
    }

    class AsyncUpdateTask{
        String path;
        int id;
    }

    class PlayListOptionDialog extends AlertDialog.Builder{
        final int MSG_UPDATE=0;
        final int MSG_RENAME=1;
        final int MSG_SETCOVER=2;
        final int MSG_INFO=3;
        final int MSG_DELETE=4;

        String playlist=null;
        PlayListItem item=null;
        int id=-1;

        PlayListOptionDialog(Context context,int index){
            super(context);
            id=index;
            item=playListItems.get(index);
            playlist=item.name;
            setTitle("歌曲列表选项:");
            setItems(new String[]{"检查更新","重命名","设置封面","列表信息","删除此列表"}, new DialogInterface.OnClickListener() {
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
                    }
                }
            });
        }

        void OnUpdate(){
            /*
            final ProgressDialog dialog = new ProgressDialog(context);
            //final ArrayList<String> list=new ArrayList<>();
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(false);
            dialog.setTitle("正在更新");
            dialog.setMessage("");
            dialog.setCanceledOnTouchOutside(false);*/
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Song[] list=dataBase.LoadPlayList(item.name);
                    /*CallerDialogUpdate dialogUpdate = new CallerDialogUpdate();
                    dialogUpdate.size = list.length;
                    dialogUpdate.dialog = dialog;*/
                    int i=0;
                    Song[] newSongs=new Song[list.length];
                    for (Song song : list) {
                        Song info = new Song();
                        SongInfoParser parser = new SongInfoParser(song.AbsFile_Path);
                        info.AbsFile_Path = song.AbsFile_Path;
                        info.Artist = parser.getArtist();
                        info.Album = parser.getAlbum();
                        info.Title = parser.getTitle();
                        newSongs[i]=info;
                        i++;
                        /*dialogUpdate.curSong=info;
                        handler.post(dialogUpdate);*/
                    }
                   /* dialog.dismiss(); */
                    dataBase.UpdatePlayList(playlist,newSongs);
                }
            }.start();
            //dialog.show();
        }
        void OnRename(){
            //// TODO: 2016/7/7
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
}
