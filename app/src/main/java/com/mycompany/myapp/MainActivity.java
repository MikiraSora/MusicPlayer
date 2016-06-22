package com.mycompany.myapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import java.util.HashMap;


public class MainActivity extends Activity 
{
	static ExMusicPlayer mplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		Init();



    }

	void Init() {
		/*
		
		*/
		Button btn = (Button) findViewById(R.id.action);
		HashMap<String, Object> map = new HashMap<String, Object>();
		Blur.setContext(getApplicationContext());

		mplayer = new ExMusicPlayer(this);
		findViewById(R.id.open).setOnClickListener(mplayer.on_default_playlistadd);
		findViewById(R.id.save).setOnClickListener(mplayer.on_default_playlistsave);
		findViewById(R.id.load).setOnClickListener(mplayer.on_default_playlistload);
		findViewById(R.id.delete).setOnClickListener((mplayer.on_default_playlistdelete));
		btn.setOnClickListener(mplayer.on_default_play);
		findViewById((R.id.Next)).setOnClickListener((mplayer.on_default_playnext));
		findViewById((R.id.Previous)).setOnClickListener((mplayer.on_default_playprevious));
		findViewById((R.id.statu)).setOnClickListener((mplayer.on_default_playmodechange));

		map.put("action", btn);
		//map.put(("visualizer"),findViewById(R.id.visualizerView));
		map.put("status", findViewById(R.id.status));
		map.put("seekbar", findViewById(R.id.mainSeekBar1));
		map.put("cover", findViewById(R.id.mainImageView1));
		map.put("song", findViewById(R.id.song));
		map.put("bg", findViewById((R.id.bg)));
		map.put("id", findViewById((R.id.id)));
		map.put("statu", findViewById((R.id.statu)));

		mplayer.map = map;
		mplayer.enableDumpDebugMsg(true);


		mplayer.initVisualizerView();
	}
}

