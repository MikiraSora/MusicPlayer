package com.mycompany.myapp.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DebugUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.AbsListView.OnScrollListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.mycompany.myapp.Cache;
import com.mycompany.myapp.MusicDataBase;
import com.mycompany.myapp.R;
import com.mycompany.myapp.Song;
import com.mycompany.myapp.SongInfoParser;
import com.mycompany.myapp.Utils;

import java.io.ByteArrayOutputStream;

/**
 * Created by mikir on 2016/7/1.
 */
public class PlayListSelection extends Activity{

    MusicDataBase musicDataBase;
    String playlist_name="";
    ListView listView;

    RelativeLayout rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        rootView=new RelativeLayout(this);
        setContentView(rootView);

        Intent intent=this.getIntent();
        playlist_name=intent.getStringExtra("playlist_name");
        if(playlist_name.length()==0){
            this.finish();
            return;
        }
        init();
    }

    private void init(){

        musicDataBase=new MusicDataBase(this);
        listView=new ListView(this);
        final Song[] songs=musicDataBase.LoadPlayList(playlist_name);
        listView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        final PlayListSelectionListAdapter adapter=new PlayListSelectionListAdapter(this, LayoutInflater.from(this));
        listView.setAdapter(adapter);

        /*
        listView.setOnScrollListener(adapter.onScrollListener);*/
        adapter.mListView=listView;

        PlayListSelectionListAdapter.PlayListInfo info=new PlayListSelectionListAdapter.PlayListInfo();
        info.Name=playlist_name;
        info.Owner="MikiraSora";
        Bitmap cover=null;
        try{
            byte[] buffer= Cache.getCacheData(playlist_name+"_cover", new Cache.OnRequestFile() {
                @Override
                public byte[] onRequestFile(String abs_path) {
                   return null;
                }
            });
            cover= BitmapFactory.decodeByteArray(buffer,0,buffer.length);
        }catch (Exception e){
            cover= null;
        }
        info.CoverImage=cover;
        final ListViewUpdateHandle handler=new ListViewUpdateHandle();

        adapter.SetHead(info);
        rootView.addView(listView);
        handler.adapter=adapter;
        handler.songs=songs;
        handler.sendEmptyMessage(handler.CHANGE);
    }
}

class ListViewUpdateHandle extends Handler{
    static final int CHANGE=1;
    PlayListSelectionListAdapter adapter;
    Song[] songs=null;
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what){
            case CHANGE:
                for(Song song:songs){
                    adapter.addItem(song);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }
}


