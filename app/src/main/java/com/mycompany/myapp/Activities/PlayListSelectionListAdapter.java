package com.mycompany.myapp.Activities;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.widget.*;
import android.content.*;

import java.lang.ref.SoftReference;
import java.util.*;
import android.view.*;
import android.graphics.*;
import android.util.*;

import com.mycompany.myapp.ExMusicPlayer;
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
    Handler handler;

    ExMusicPlayer musicPlayer=null;

    HashMap<String,Bitmap> cacheBitmap=new HashMap<>();

    ArrayList<AysncUpdateCoverTask> aysncUpdateCoverTaskArrayList=new ArrayList<>();

    ArrayList<AysncUpdateCoverTask> aysncPostUpdateTaskArrayList=new ArrayList<>();


    public final int Type_Head=0;
    public final int Type_Item=1;

    boolean isUpdateRunning=false;
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
        //syncImageLoader=new Syncimageloader();
    }

    void setMusicPlayer(ExMusicPlayer player){
        musicPlayer=player;
    }

    void addItem(Song sd)
    {
        item_list.add(sd);
        AysncUpdateCoverTask ntask=new AysncUpdateCoverTask();
        ntask._id=item_list.size()-1;
        ntask.path=sd.AbsFile_Path;
        aysncUpdateCoverTaskArrayList.add(ntask);
        if(isUpdateRunning)
            return;
        isUpdateRunning=true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    if(aysncUpdateCoverTaskArrayList.size()!=0){
                        final PlayListSelectionListAdapter.AysncUpdateCoverTask task= aysncUpdateCoverTaskArrayList.remove(0);
                        if(!cacheBitmap.containsKey(task.path)){
                            cacheBitmap.put(task.path,(Utils.getCover(task.path)));
                        }
                    }else {
                        break;
                    }
                }
                isUpdateRunning=false;
            }
        }).start();
    }

    public void PostChangeCache(){
        //cacheBitmap=syncImageLoader.imagecache;
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
    public View getView(int p1, View p2, ViewGroup p3)
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
        try
        {
            Bitmap bmp=null;
            PlayListInfo fd=null;
            Song sd=null;
            ImageView imageView=null;
            TextView textView=null;

            switch (getItemViewType(p1))
            {
                case Type_Head:
                    fd = headInfo;
                    imageView=(ImageView)rl.findViewById(R.id.folder_bg);
                    ((TextView)rl.findViewById(R.id.folder_name)).setText(headInfo.Name);
                    ((TextView)rl.findViewById(R.id.folder_owner)).setText(headInfo.Owner);
                    if (headInfo.CoverImage==null)
                    {
                        rl.setTag(p1);
                        //imageView.setImageBitmap(item_list.size()!=0?getCover(item_list.get(0).AbsFile_Path):null);
                        AsyncSetImageBitmap(p1,item_list.get(0).AbsFile_Path);
                    }
                    else
                    {
                        imageView.setImageBitmap(fd.CoverImage);
                    }
                    break;
                case Type_Item:
                    sd = (Song) item_list.get((int)getItemId(p1));
                    ((TextView)rl.findViewById(R.id.song_artist)).setText(sd.Artist);
                    ((TextView)rl.findViewById(R.id.song_tittle)).setText(sd.Title);
                    rl.setTag(p1);
                    AsyncSetImageBitmap(p1,sd.AbsFile_Path);
            }
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
        if(!cacheBitmap.containsKey(path)){
            Bitmap bmp=Utils.getCover(path);
            cacheBitmap.put(path,(bmp));
            return bmp;
        }
        return cacheBitmap.get(path);
    }

    void AsyncSetImageBitmap(final int position, final String path){
        AysncUpdateCoverTask ntask=new AysncUpdateCoverTask();
        ntask._id=position;
        ntask.path=path;
        aysncPostUpdateTaskArrayList.add(ntask);

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
                            if(aysncPostUpdateTaskArrayList.size()==0)
                                break;
                            AysncUpdateCoverTask task = aysncPostUpdateTaskArrayList.remove(0);
                            if (mListView == null)
                                return;
                            RelativeLayout rl = (RelativeLayout) mListView.findViewWithTag(task._id);
                            if (rl == null)
                                return;
                            ImageView iv = (ImageView) rl.findViewById(task._id == 0 ? R.id.folder_bg : R.id.song_cover);
                            if (iv == null)
                                return;
                            iv.setImageBitmap(getCover(task.path));
                            Log.i("AysncUpdateCoverTask", String.format("%d - %s is automatic calling", task._id, task.path));
                        }
                    }
                });
                isPostUpdateRunning=false;
            }
        }).start();
    }

    public static class PlayListInfo{
        public String Name,Owner;
        public Bitmap CoverImage=null;
    }

    class AysncUpdateCoverTask{
        int _id;
        String path;
    }

}