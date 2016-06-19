package com.mycompany.myapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;


public class MainActivity extends Activity 
{
	private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
	static ExMusicPlayer mplayer;
	OpenFileDialog file_dialog;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		file_dialog = new OpenFileDialog(MainActivity.this);
		file_dialog.setCancelable(false);
		file_dialog.setOkButtonText("确定");
		file_dialog.setPath("/sdcard/");
		file_dialog.setFolderSelectable(true)
				.setFolderIcon(R.drawable.ic_folder_black_48dp)
				.setFileIcon(R.drawable.ic_insert_drive_file_black_48dp)
				.setTitle("请选择音乐文件夹......");
		file_dialog.setOnCloseListener(new onSelectFolder());

		findViewById(R.id.open).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View p1) {
				file_dialog.show();
			}
		});

		MusicDataBase dataBase = new MusicDataBase(getApplicationContext());

		Random rand = new Random();
		Song song;
		for (int i = 0; i < 50; i++) {
			song = new Song();
			song.Title = Integer.toString(rand.nextInt(1000));
			song.Artist = Integer.toString(rand.nextInt(1000));
			song.Album = Integer.toString(rand.nextInt(1000));
			song.AbsFile_Path = Integer.toString(rand.nextInt(1000));
			song.File_Path = Integer.toString(rand.nextInt(6));
			dataBase.Add(song);
		}


		Song[] Result = dataBase.GetSongsFromPlayList("5");

		for (Song s : Result)
			Log.i("test", String.format("\"%s - %s\"", s.Title, s.Artist));

    }

	void Init(String[] g) {
		/*
		
		*/

		mplayer = new ExMusicPlayer(this, g/*new String[]{
								   "/mnt/media_rw/sdcard1/netease/cloudmusic/Music/美郷あき - Glitter.mp3","/sdcard/hanagoyomi.mp3","/sdcard/WindClimbing.mp3"
								   }*/);
		Button btn = (Button) findViewById(R.id.action);
		HashMap<String, Object> map = new HashMap<String, Object>();
		Blur.setContext(getApplicationContext());
		btn.setOnClickListener(mplayer.on_default_play);
		findViewById((R.id.Next)).setOnClickListener((mplayer.on_default_playnext));
		findViewById((R.id.Previous)).setOnClickListener((mplayer.on_default_playprevious));
		findViewById((R.id.statu)).setOnClickListener((mplayer.on_default_playmodechange));
		findViewById(R.id.save).setOnClickListener(mplayer.on_default_changeplaylistsave);

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
		mplayer.enableDumpDebugMsg(false);


		mplayer.initVisualizerView();
	}


	class onSelectFolder implements OpenFileDialog.OnCloseListener {

		@Override
		public void onOk(String selectedFile) {
			File file = new File(selectedFile);
			ArrayList<String> list = new ArrayList<String>();
			if (!file.isDirectory())
				return;
			File[] files = file.listFiles();

			Pattern reg = Pattern.compile(".*\\.mp3");

			int i = 0;
			for (File f : files) {
				if (f.isDirectory())
					continue;
				if (!reg.matcher(f.getAbsolutePath()).matches())
					continue;
				list.add(f.getAbsolutePath());
				i++;
			}
			String[] g = new String[list.size()];
			i = 0;
			for (String d : list) {
				g[i] = d;
				i++;
			}
			if (list.size() != 0)
				Init(g);
			else {
				file_dialog.setTitle("请选择正确的文件夹！");
				file_dialog.show();
			}
		}

		@Override
		public void onCancel() {

		}
	}

	/*
	@TargetApi(23)
    private void checkForPermission(){
        if(PackageManager.PERMISSION_GRANTED!= checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                //Toast.makeText(getApplicationContext(), R.string.permission.request_read_external_storage, Toast.LENGTH_LONG).show();
            }else {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
					if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

						Intent intent = getIntent();
						PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
						AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
						am.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
						System.exit(0);
					} else {
						new AlertDialog.Builder(this).setMessage(R.string.permission_request_read_external_storage_denied)
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.this.finish();
                                }
                            })
                            .create()
                            .show();
					}
					return;
				}
        }
    }*/
}

