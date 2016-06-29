package com.mycompany.myapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;

import com.mycompany.myapp.LyricView.LyricView;

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

		final LyricView lyricView = (LyricView) findViewById(R.id.lyricview);
		Button btn = (Button) findViewById(R.id.action);
		HashMap<String, Object> map = new HashMap<String, Object>();
		Blur.setContext(getApplicationContext());

		mplayer = new ExMusicPlayer(this);
		mplayer.setPlayPauseButton((Button) findViewById(R.id.action), mplayer.on_default_play, mplayer.on_default_pause, mplayer.on_default_resume);
		mplayer.setPlayNextButton((Button) findViewById(R.id.Next), mplayer.on_default_playnext);
		mplayer.setPlayPreviousButton((Button) findViewById(R.id.Previous), mplayer.on_default_playprevious);
		mplayer.setChangePlayModeButton((Button) findViewById(R.id.statu), mplayer.on_default_playmodechange);
		mplayer.setPlayListSaveButton((Button) findViewById(R.id.save), mplayer.on_default_playlistsave);
		mplayer.setPlayListDeleteButton((Button) findViewById(R.id.delete), mplayer.on_default_playlistdelete);
		mplayer.setPlayListLoadButton((Button) findViewById(R.id.load), mplayer.on_default_playlistload);
		mplayer.setPlayListAddButton((Button) findViewById(R.id.open), mplayer.on_default_playlistadd);
		mplayer.setSeekTimeBar((SeekBar) findViewById(R.id.mainSeekBar1), mplayer.on_default_seektime);

		map.put("status", findViewById(R.id.status));
		map.put("seekbar", findViewById(R.id.mainSeekBar1));
		map.put("cover", findViewById(R.id.mainImageView1));
		map.put("song", findViewById(R.id.song));
		map.put("bg", findViewById((R.id.bg)));
		map.put("id", findViewById((R.id.id)));
		map.put("statu", findViewById((R.id.statu)));
		map.put("lyricview", findViewById((R.id.lyricview)));

		mplayer.map.putAll(map);
		mplayer.enableDumpDebugMsg(true);

		lyricView.setOnScrollFhinished(new LyricView.OnScrollFinished() {
			@Override
			public void onScrolliFinished(int time) {
				mplayer.JumpTo(time, 0, false);
			}
		});
		lyricView.bindMusicPlayer(mplayer);
		lyricView.Hide();
		lyricView.setOnRequestShowOrHide(new LyricView.OnRequestShowOrHide() {
			@Override
			public void onRequestShowOrHide() {
				if (lyricView.isHide()) {
					lyricView.Show();
					findViewById(R.id.mainImageView1).setVisibility(View.INVISIBLE);
				} else {
					lyricView.Hide();
					findViewById(R.id.mainImageView1).setVisibility(View.VISIBLE);
				}

			}
		});
		Cache.Init(this);
		mplayer.bindLyricView(lyricView);
	}
}

