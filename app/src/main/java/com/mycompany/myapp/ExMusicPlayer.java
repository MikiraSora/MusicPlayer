package com.mycompany.myapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
//import java.util.logging.*;

class ExMusicPlayer extends MusicPlayer {
    HashMap<String, Object> map;
    Thread t = new Thread(new UpdateTimeCaller());
    TimerDisplayer displayer = new TimerDisplayer(new Handler());

    MusicDataBase musicDataBase = null;

    TriggerPlay on_default_play = new TriggerPlay();
    TriggerPauseMusic on_default_pause = new TriggerPauseMusic();
    TriggerResumeMusic on_default_resume = new TriggerResumeMusic();
    TriggerStop on_default_stop = new TriggerStop();
    TriggerPlayNext on_default_playnext = new TriggerPlayNext();
    TriggerPlayPrevious on_default_playprevious = new TriggerPlayPrevious();
    TriggerChangePlayMode on_default_playmodechange = new TriggerChangePlayMode();
    TriggerPlayListSave on_default_playlistsave = new TriggerPlayListSave();
    TriggerPlayListLoad on_default_playlistload = new TriggerPlayListLoad();
    TriggerPlayListDelete on_default_playlistdelete = new TriggerPlayListDelete();
    TriggerPlayListAdd on_default_playlistadd = new TriggerPlayListAdd();


    boolean ableUpdateTime = true;
    boolean alreayStarted = false;

    String playlist_name = null;

    //VisualizerView visualizer=null;

    protected ExMusicPlayer(Context c) {
        this(c, null);
    }

    public ExMusicPlayer(Context c, String[] list) {
        super(c, new Handler(), list);

        setOnPlay(new OnPlay() {
            boolean isfirst = true;

            @Override
            public void onPlay(int index) {
                ((Button) map.get("action")).setText("Playing");
                StartUpdate();
            }
        });
        setPlayChangeMode(PlayChangeMode.Normal);
        setOnGetInfo(new TriggerOnGetInfo());
        setOnChangePlayMode(new CallerPlayModeChange());
        musicDataBase = new MusicDataBase(c);
    }

    void initVisualizerView() {/*
        VisualizerView visualizer=(VisualizerView)map.get("visualizer");
		visualizer.enableAsyncRender(true);
		Paint linePaint = new Paint();
		linePaint.setStrokeWidth(1f);
		linePaint.setAntiAlias(true);
		linePaint.setColor(Color.argb(88, 0, 128, 255));

		Paint lineFlashPaint = new Paint();
		lineFlashPaint.setStrokeWidth(5f);
		lineFlashPaint.setAntiAlias(true);
		lineFlashPaint.setColor(Color.argb(188, 255, 255, 255));
		LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint, true);
		visualizer.addRenderer(lineRenderer);
		//psetVisualizerView(visualizer);
		visualizer.enableAsyncRender(false);
		visualizer.setAsyncHandler(new Handler());*/
    }

    private void StartUpdate() {
        if (alreayStarted)
            return;
        alreayStarted = true;
        ((SeekBar) map.get("seekbar")).setOnSeekBarChangeListener(new TriggerSeekTime());
        t.start();
    }

    class TriggerOnGetInfo implements OnGetInfo {
        @Override
        public void onGetInfo(MusicPlayer.SongInfo info) {
            handle.post(new RenderBG(info.Cover));
            if (info.Cover == null) {
                ((ImageView) map.get("cover")).setImageBitmap(null);
            } else {
                ((ImageView) map.get("cover")).setImageBitmap(info.Cover);
            }
            ((TextView) map.get("id")).setText(String.format("%s / %s", info.Index + 1, play_list.size()));
            ((TextView) map.get("song")).setText(String.format("%s - %s", info.Artist, info.Title));
            Log.d("song info", String.format("%s - %d : %s - %s", info.Encode, info.Index, info.Artist, info.Title));
            Log.d("PlayStatu", "onGetInfo()");
        }

    }

    class RenderBG implements Runnable {
        Bitmap src = null;

        private RenderBG() {
        }

        public RenderBG(Bitmap _src) {
            src = _src;
        }

        @Override
        public void run() {
            try {
                ImageView iv = (ImageView) map.get("bg");
                if (src == null)
                    iv.setImageBitmap(null);
                else
                    iv.setImageBitmap(Blur.DoBlurWithScale(src, 25.0f, 0.4f));
            } catch (Exception e) {
                SendErrorMsg(e.getMessage());
            }
        }
    }

    class UpdateTimeCaller implements Runnable {
        public UpdateTimeCaller() {
        }

        @Override
        public void run() {
            displayer.notifyThreadCall();
        }
    }

    public class TriggerPlay implements OnClickListener {
        @Override
        public void onClick(View p1) {
            ((Button) map.get("action")).setText("Playing");
            ((Button) map.get("action")).setOnClickListener(new TriggerPauseMusic());

            Play(index);
            StartUpdate();
        }
    }

    class TriggerResumeMusic implements OnClickListener {
        @Override
        public void onClick(View p1) {
            ((Button) map.get("action")).setText("Playing");
            ((Button) map.get("action")).setOnClickListener(new TriggerPauseMusic());

            Resume();
        }
    }

    class TriggerPauseMusic implements OnClickListener {
        @Override
        public void onClick(View p1) {
            ((Button) map.get("action")).setText("Pause");
            ((Button) map.get("action")).setOnClickListener(new TriggerResumeMusic());

            Pause();
        }
    }

    class TriggerPlayNext implements OnClickListener {
        @Override
        public void onClick(View p1) {
            PlayNext();
        }
    }

    class TriggerChangePlayMode implements OnClickListener {
        @Override
        public void onClick(View p1) {
            if (mode == PlayChangeMode.Loop) {
                setPlayChangeMode(PlayChangeMode.Shuffle);
                return;
            }
            if (mode == PlayChangeMode.Shuffle) {
                setPlayChangeMode(PlayChangeMode.Normal);
                return;
            }
            if (mode == PlayChangeMode.Normal) {
                setPlayChangeMode(PlayChangeMode.Loop);
                return;
            }
        }
    }

    class CallerPlayModeChange implements OnPlayModeChange {
        @Override
        public void OnPlayModeChange(MusicPlayer.PlayChangeMode _mode) {
            if (_mode == PlayChangeMode.Loop)
                ((Button) map.get("statu")).setText("Loop");
            if (_mode == PlayChangeMode.Shuffle)
                ((Button) map.get("statu")).setText("Shuffle");
            if (_mode == PlayChangeMode.Normal)
                ((Button) map.get("statu")).setText("Normal");
            Log.d("PlayStatu", "onPlayModeChange()");
        }
    }

    class TriggerPlayPrevious implements OnClickListener {
        TimeoutCaller caller = new TimeoutCaller();
        Handler h = new Handler();
        int reflect_time = 500, rec = -1;

        @Override
        public void onClick(View p1) {
            if (rec < 0) {
                rec = getCurrentTimeNow();
                h.postDelayed(caller, reflect_time);
            } else {
                Integer offset = getCurrentTimeNow() - rec;
                if (offset < reflect_time) {
                    PlayPrevious();
                    rec = -1;
                }
            }
        }

        class TimeoutCaller implements Runnable {
            Object locker = new Object();

            @Override
            public void run() {
                try {
                    if (rec >= 0)
                        Replay();
                } catch (Exception e) {
                    e.fillInStackTrace();
                    SendErrorMsg(e.getMessage());
                } finally {
                    rec = -1;
                }
            }
        }
    }

    class TimerDisplayer {
        Handler handler;
        boolean isExit = false;

        public TimerDisplayer(Handler _h) {
            handler = _h;
        }

        public void notifyThreadCall() {
            try {
                while (!isExit) {
                    Thread.currentThread().sleep(1000, 0);
                    notifyUpdate();
                }
            } catch (Exception e) {
                SendErrorMsg(e.getMessage());
            }
        }

        public void notifyUpdate() {
            handler.post(new UpdateTime());
        }

        public void notifySeekUpdate(int cur_time) {
            int min, sec, max_min, max_sec;
            int t = getSongLength();
            max_min = t / 1000 / 60;
            max_sec = (t - max_min * 60000) / 1000;
            t = cur_time;
            min = t / 1000 / 60;
            sec = (t - min * 60000) / 1000;
            ((TextView) map.get("status")).setText(String.format("%d:%d - %d:%d", min, sec, max_min, max_sec));
        }
    }

    class UpdateTime implements Runnable {
        @Override
        public void run() {
            int min, sec, max_min, max_sec;
            if (ableUpdateTime) {
                ((SeekBar) map.get("seekbar")).setProgress((int) (((getCurrentTimeNow() * 1.0f) / getSongLength()) * 100));
            }
            int t = getSongLength();
            max_min = t / 1000 / 60;
            max_sec = (t - max_min * 60000) / 1000;
            t = getCurrentTimeNow();
            min = t / 1000 / 60;
            sec = (t - min * 60000) / 1000;
            ((TextView) map.get("status")).setText(String.format("%d:%d - %d:%d", min, sec, max_min, max_sec));
        }
    }

    class TriggerSeekTime implements OnSeekBarChangeListener {
        int sum = 0;

        @Override
        public void onStartTrackingTouch(SeekBar p1) {
            ableUpdateTime = false;
        }

        @Override
        public void onStopTrackingTouch(SeekBar p1) {
            int pos = p1.getProgress() == 100 ? getSongLength() - 1 : (int) (p1.getProgress() / 100.0f * getSongLength());
            JumpTo(pos, 0, false);
            ableUpdateTime = true;
        }

        @Override
        public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
            int pos = p1.getProgress() == 100 ? getSongLength() - 1 : (int) (p1.getProgress() / 100.0f * getSongLength());
            displayer.notifySeekUpdate(pos);
        }
    }

    class TriggerStop implements OnClickListener {
        @Override
        public void onClick(View p1) {
            ((TextView) map.get("status")).setText("Stop");
            Stop();
        }
    }

    class TriggerPlayListSave implements OnClickListener {
        @Override
        public void onClick(View view) {
            final Song[] saves = new Song[play_list.size()];
            play_list.toArray(saves);
            if (playlist_name == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                final EditText editText = new EditText(ctx);
                editText.setHint("input new playlist name.");
                builder.setMessage("Save to new playlist.");
                builder.setView(editText);
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        playlist_name = editText.getText().toString();
                        if (playlist_name.length() == 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                            builder.setTitle("新建的播放列表名不能为空!");
                            builder.setPositiveButton("了解", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                        } else
                            musicDataBase.SavePlayList(playlist_name, saves);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setMessage("已存在播放列表，是否更新?");
                builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!musicDataBase.UpdatePlayList(playlist_name, saves)) {
                            SendErrorMsg("musicdatabase cant update curtten playlist because of missing.");
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.setNeutralButton("New Another", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        playlist_name = null;
                        on_default_playlistadd.onClick(null);
                    }
                });
                builder.show();
            }
        }
    }

    class TriggerPlayListLoad implements OnClickListener {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("请选择播放列表");
            //final PlaylistSelectionListViewer listViewer = new PlaylistSelectionListViewer(ctx);

            if (musicDataBase.getAllPlayList().length != 0) {
                final String[] allPlayList = musicDataBase.getAllPlayList();
                builder.setItems(allPlayList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Song[] songs = musicDataBase.LoadPlayList(allPlayList[i]);
                        if (songs == null) {
                            SendErrorMsg("Cant get any song from playlist because of returning null");
                            return;
                        } else if (songs.length == 0) {
                            SendErrorMsg("Cant get any song from playlist because nothing in it");
                            return;
                        }
                        play_list.clear();
                        for (Song song : songs)
                            play_list.add(song);
                        Reset();
                    }
                });
            } else {
                builder.setTitle("无任何播放列表记录在内!");
                builder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
            }
                /*
                 for (String name : musicDataBase.getAllPlayList())
                     listViewer.addPlayListName(name);
                builder.setView(listViewer);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                  @Override
                   public void onClick(DialogInterface dialogInterface, int i) {

                  }
                  });
                builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = listViewer.getResult();
                        if(name==null)
                            return;
                        Song[] songs = musicDataBase.LoadPlayList(name);
                        if (songs == null){
                         SendErrorMsg("Cant get any song from playlist because of returning null");
                         return;
                         }
                        else if (songs.length == 0){
                            SendErrorMsg("Cant get any song from playlist because nothing in it");
                            return;
                        }
                        play_list.clear();
                        for (Song song : songs)
                            play_list.add(song);
                        Reset();
                    }
              });
            }else{
                builder.setMessage("无任何播放列表!");
                builder.setNeutralButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });
            }
               */
            builder.show();
        }
    }

    class TriggerPlayListDelete implements OnClickListener {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("请选择播放列表");
            final PlaylistSelectionListViewer listViewer = new PlaylistSelectionListViewer(ctx);

            if (musicDataBase.getAllPlayList().length != 0) {
                for (String name : musicDataBase.getAllPlayList())
                    listViewer.addPlayListName(name);
                builder.setView(listViewer);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = listViewer.getResult();
                        if (name == null)
                            return;
                        musicDataBase.DeletePlayList(name);
                        Reset();
                    }
                });
            } else {
                builder.setMessage("无任何播放列表!");
                builder.setNeutralButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
            }

            builder.show();
        }
    }

    class TriggerPlayListAdd implements OnClickListener {
        OpenFileDialog file_dialog;

        @Override
        public void onClick(View view) {
            file_dialog = new OpenFileDialog(ctx);
            file_dialog.setOkButtonText("确定");
            file_dialog.setPath("/sdcard/");
            file_dialog.setFolderSelectable(true)
                    .setFolderIcon(R.drawable.ic_folder_black_48dp)
                    .setFileIcon(R.drawable.ic_insert_drive_file_black_48dp)
                    .setTitle("请选择音乐文件夹......");
            file_dialog.setOnCloseListener(new onSelectFolder());
            file_dialog.show();
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
                if (list.size() != 0) {
                    AddSong(list);
                } else {
                    file_dialog.setTitle("请选择正确的文件夹！");
                    file_dialog.show();
                }
            }

            private void AddSong(final ArrayList<String> list) {
                final ProgressDialog dialog = new ProgressDialog(ctx);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setCancelable(false);
                dialog.setTitle("正在添加");
                dialog.setMessage("");
                dialog.setCanceledOnTouchOutside(false);
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        CallerDialogUpdate dialogUpdate = new CallerDialogUpdate();
                        dialogUpdate.size = list.size();
                        dialogUpdate.dialog = dialog;
                        for (String str : list) {
                            dialogUpdate.curSong = addSong(str);
                            handle.post(dialogUpdate);
                        }
                        dialog.dismiss();
                    }
                }.start();
                dialog.show();
            }

            @Override
            public void onCancel() {
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
}
