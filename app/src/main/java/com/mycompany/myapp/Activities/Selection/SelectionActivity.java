package com.mycompany.myapp.Activities.Selection;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mycompany.myapp.Activities.PlayListSelectionListAdapter;
import com.mycompany.myapp.Activities.Selection.SelectionAdapter.AlbumAdapter;
import com.mycompany.myapp.Activities.Selection.SelectionAdapter.PlayListAdapter;
import com.mycompany.myapp.CacheCollection.BitmapCache;
import com.mycompany.myapp.CacheCollection.MemoryCache;
import com.mycompany.myapp.ExMusicPlayer;
//import com.mycompany.myapp.MemoryCache;
import com.mycompany.myapp.MusicDataBase;
import com.mycompany.myapp.ParameterSender;
import com.mycompany.myapp.R;
import com.mycompany.myapp.Song;
import com.mycompany.myapp.Utils;

/**
 * Created by mikir on 2016/7/5.
 */
public class SelectionActivity extends Activity {

    ViewPager viewPager=null;
    PagerTabStrip pagerTabStrip=null;
    SelectionViewPagerTabAdapter adapter=null;
    MusicDataBase musicDataBase=null;

    ExMusicPlayer musicPlayer=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_selection);
        ((ParameterSender)this.getApplication()).putObject("BitmapCache",new BitmapCache());

        viewPager=(ViewPager)findViewById(R.id.viewpager);
        //pagerTabStrip=(PagerTabStrip)findViewById(R.id.tabstrip);
        PagerTitleStrip pagerTitleStrip=(PagerTitleStrip)findViewById(R.id.titlestrip);
        pagerTitleStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP,19.0f);

        adapter=new SelectionViewPagerTabAdapter();

        if(viewPager.getAdapter()==null){
            Log.i("ViewPager","No adapter!");
        }


        musicPlayer=(ExMusicPlayer)((ParameterSender)getApplicationContext()).getObject("musicplayer");
/*
        //取消tab下面的长横线
        pagerTabStrip.setDrawFullUnderline(true);
        //设置tab的背景色
        pagerTabStrip.setBackgroundColor(Color.rgb(0,255,255));
        //设置当前tab页签的下划线颜色
        pagerTabStrip.setTabIndicatorColor(Color.rgb(0,255,0));
        pagerTabStrip.setTextSpacing(200);
*/
        musicDataBase=new MusicDataBase(this);



        AddPlaylistTab();
        AddAlbumTab();
        AddArtistTab();
        AddAllMusicTab();
        AddFolderTab();
        viewPager.setAdapter(adapter);
        Log.i("Selection","init finish!");
    }

    void AddPlaylistTab(){
        SelectionViewPagerTabAdapter.TabViewItem viewItem=new SelectionViewPagerTabAdapter.TabViewItem();
        viewItem.title="播放列表";

        ListView listView=new ListView(this);
        String[] playlists=musicDataBase.getAllPlayList();

        PlayListAdapter playListAdapter=new PlayListAdapter(this,listView,musicPlayer);
        listView.setAdapter(playListAdapter);
        final PlayListAdapter.SeekUpdater seekUpdater=new PlayListAdapter.SeekUpdater(playListAdapter,listView);
        listView.setOnScrollListener(seekUpdater);

        if(playlists.length==0){
            TextView textView=new TextView(this);
            textView.setText("无任何播放列表");
            textView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            viewItem.view=textView;
            adapter.add(viewItem);
            return;
        }
        Song[] head_songs=new Song[playlists.length];
        for(int i=0;i<playlists.length;i++){
            head_songs[i]=musicDataBase.getFirstSongOfPlayList(playlists[i]);
            PlayListAdapter.PlayListItem info=new PlayListAdapter.PlayListItem();
            info.name=playlists[i];
            info.firstSong=head_songs[i];
            playListAdapter.addItem(info);
        }

        viewItem.view=listView;
        adapter.add(viewItem);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                seekUpdater.updateItem();
            }
        },100);
    }
    void AddAllMusicTab(){
        //// TODO: 2016/7/7
    }
    void AddAlbumTab(){
        String[] album_collection=musicDataBase.getAllAlbum();
        SelectionViewPagerTabAdapter.TabViewItem viewItem=new SelectionViewPagerTabAdapter.TabViewItem();
        viewItem.title="专辑列表";
        if(album_collection==null){
            TextView textView=new TextView(this);
            textView.setText("无任何专辑记录在内!");
            textView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            viewItem.view=textView;
            adapter.add(viewItem);
            return;
        }
        Song[] head_songs=new Song[album_collection.length];
        Song song;

        ListView listView=new ListView(this);
        AlbumAdapter albumAdapter=new AlbumAdapter(this,listView);
        final AlbumAdapter.SeekUpdater seekUpdater=new AlbumAdapter.SeekUpdater(albumAdapter,listView);
        listView.setOnScrollListener(seekUpdater);
        listView.setAdapter(albumAdapter);

        for(int i=0;i<head_songs.length;i++){
            song=musicDataBase.getFirstSongOfAlbum(album_collection[i]);
            if(song==null)
                continue;
            albumAdapter.addItem(song);
            //Log.i("AlbumAdapter",String.format("load album %s",song.Album));
        }

        viewItem.view=listView;
        adapter.add(viewItem);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                seekUpdater.updateItem();
            }
        },100);
        }
    void AddArtistTab(){}
    void AddFolderTab(){}

    void ExitWithCause(String cause){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(cause);
        builder.setNegativeButton("Return", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SelectionActivity.this.finish();
            }
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
