package com.mycompany.myapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

//oimport java.util.logging.*;

public class MusicPlayer {
    protected Context ctx;
    protected ArrayList<Song> play_list = new ArrayList<Song>();
    protected int index;
    protected Handler handle;
    protected PlayChangeMode mode = PlayChangeMode.Normal;
    private MediaPlayer player;
    private boolean ableSendErrorMsg = false;
    private PlayerStatu status = PlayerStatu.Idle;
    private boolean isPause = false;
    private boolean isLoopCurttenPlayList = true;
    private int prev_pos = 0;
    private OnPlay trigger_onplay = null;
    private OnGetInfo trigger_ongetinfo = null;
    private OnError trigger_onerror = null;
    private OnPause trigger_onpause = null;
    private OnPlayModeChange trigger_onplaymodechange = null;
    private OnStop trigger_onstop = null;
    private OnPlayFirstTime trigger_onplayfirsttime = null;
    private OnReset trigger_onreset = null;
    private OnResume trigger_onresume = null;
    private PlayHistoryRecoder playrecorder = new PlayHistoryRecoder();

    protected MusicPlayer() {
    }

    public MusicPlayer(Context _ctx, Handler _handle, String[] _play_list) {
        ctx = _ctx;
        handle = _handle;
        if (_play_list != null)
            for (String _t : _play_list)
                play_list.add(AddSongToCurrentPlayList(_t));
        initPlayer();
    }

    private void initPlayer() {
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnErrorListener(new TriggerPlayerError());
        player.setOnCompletionListener(new TriggerPlayerComplete());
        player.setOnPreparedListener(new PreparedPlay());
    }

    //feature
    void enableDumpDebugMsg(boolean _switch) {
        ableSendErrorMsg = _switch;
    }

    Song addSong(String path) {
        Song song = AddSongToCurrentPlayList(path);
        play_list.add(song);
        return song;
    }

    public void Play(int _index) {
        try {
            /*if ((status == PlayerStatu.Error) || (status == PlayerStatu.Processing))
                player.reset();*/
            //throw new Exception("Player isnt Idle now");
            if ((_index >= play_list.size())) {
                SendErrorMsg("Play a track which unreachable : " + _index);
                index = play_list.size() - 1;
                return;
            }
            if (_index < 0) {
                SendErrorMsg("Play a track which unreachable : " + _index);
                index = 0;
                Play(index);
                return;
            }
            /*
            if (((status == PlayerStatu.Playing) || (status == PlayerStatu.Pause))) {
                player.reset();
            }*/
            player.reset();
            index = _index;
            String song_path = play_list.get(index).AbsFile_Path;
            if(!(new File(song_path).exists())){
                SendErrorMsg(String.format("mp3 file %s is not found!"));
                status=PlayerStatu.Error;
            }
            player.setDataSource(song_path);

            isPause = status == PlayerStatu.Pause;
            status = PlayerStatu.Processing;
            handle.post(new CallerGetSongInfo());

            player.prepareAsync();
        } catch (Exception e) {
            e.fillInStackTrace();
            SendErrorMsg(e.getMessage());
            status = PlayerStatu.Error;
        }
    }

    void PlayFirstTime() {
        Play(0);
        if (trigger_onplayfirsttime != null)
            trigger_onplayfirsttime.OnPlayFirstTime(index);
    }

    protected MediaPlayer getPlayer() {
        return player;
    }

    void PlayPrevious() {
        Play(playrecorder.getPrev());
    }

    void PlayNext() {
        if (mode == PlayChangeMode.Loop) {
            Play(index);
            return;
        }
        /*
		if(mode==PlayChangeMode.Shuffle){
			Random rand=new Random();
			Play(rand.nextInt(play_list.size()-1));
			return;
		}*/
        int i = playrecorder.getNext();
        if (i == -1)
            if (!isLoopCurttenPlayList)
                Stop();
            else
                Play(0);
        else
            Play(i);
    }

    void Replay() {
        player.seekTo(0);
    }

    void Reset(){
        //player.reset();
        //status=PlayerStatu.Idle;
        index=0;
        playrecorder.Reset();
    }

    void Pause() {
        if (!(status == PlayerStatu.Playing)) {
            SendErrorMsg("Player is not playing");
            return;
        }
        player.pause();
        status = PlayerStatu.Pause;
        if (trigger_onpause != null)
            trigger_onpause.onPause();
    }

    void Resume() {
        if (!(status == PlayerStatu.Pause)) {
            SendErrorMsg("Player is not pause");
            return;
        }
        player.start();
        status = PlayerStatu.Playing;
        if (trigger_onresume != null)
            trigger_onresume.onResume(index);
    }

    void JumpTo(int time, int offset, boolean isPlayNow) {
        if (!((status == PlayerStatu.Pause) || (status == PlayerStatu.Playing))) {
            SendErrorMsg("Player is not pause or playing");
            return;
        }
        if (time == 0)
            if (offset != 0)
                time = player.getCurrentPosition();
        if ((time + offset) >= player.getDuration()) {
            SendErrorMsg("Jump to unreachable timepos");
            return;
        }
        player.seekTo(time + offset);
    }

    void enableLoopCurttenPlayList(boolean b) {
        isLoopCurttenPlayList = b;
    }

    void Stop() {
        if (!((status == PlayerStatu.Playing) || (status == PlayerStatu.Pause))) {
            SendErrorMsg("player isnt Playing or Pause");
            return;
        }

        player.stop();
        player.release();
        status = PlayerStatu.Stop;
        if (trigger_onstop != null)
            trigger_onstop.onStop(OnStop.Cause.PlayFinish, "play end of playlist in normal play mode");
    }

    boolean IsPause() {
        return status == PlayerStatu.Pause;
    }

    int getPlayListSize() {
        return play_list.size();
    }

    void setPlayChangeMode(PlayChangeMode _mode) {
        mode = _mode;
        if (trigger_onplaymodechange != null)
            trigger_onplaymodechange.OnPlayModeChange(mode);
    }

    void SendErrorMsg(String msg) {
        if (ableSendErrorMsg) {
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
            //Log.d("MusicPlayer", msg);
            if (trigger_onerror != null)
                trigger_onerror.onError(msg);
        }
    }

    int getSongLength() {
        return this.player.getDuration();
    }

    public int getCurrentTimeNow() {
        if (player.isPlaying()) {
            prev_pos = player.getCurrentPosition();
        }
        return prev_pos;
    }

    void setOnPlay(OnPlay e) {
        this.trigger_onplay = e;
    }

    void setOnPause(OnPause e) {
        this.trigger_onpause = e;
    }

    void setOnError(OnError e) {
        this.trigger_onerror = e;
    }

    void setOnGetInfo(OnGetInfo e) {
        this.trigger_ongetinfo = e;
    }

    void setOnChangePlayMode(OnPlayModeChange e) {
        this.trigger_onplaymodechange = e;
    }

    void setOnPlayFirstTime(OnPlayFirstTime e) {
        this.trigger_onplayfirsttime = e;
    }

    void setOnReset(OnReset e) {
        this.trigger_onreset = e;
    }

    void setOnStop(OnStop e) {
        this.trigger_onstop = e;
    }

    void setOnResume(OnResume e) {
        this.trigger_onresume = e;
    }

    private Song AddSongToCurrentPlayList(String file_path) {
        Song info = new Song();
        SongInfoParser parser = new SongInfoParser(file_path);
        info.AbsFile_Path = file_path;
        info.Artist = parser.getArtist();
        info.Album = parser.getAlbum();
        info.Title = parser.getTitle();
        return info;
    }

    private Song getSongInfo(int _index) {
        Song info =play_list.get(_index);
        info.PlayListId=_index;
        info.Cover=Utils.getCover(info.AbsFile_Path);
        return info;
    }

    public enum PlayChangeMode {
        Normal,
        Shuffle,
        Loop
    }

    enum PlayerStatu {
        Playing,
        Pause,
        Stop,

        Processing,
        Idle,
        Error
    }

    //CallBack
    public interface OnPlay {
        void onPlay(int index);
    }

    public interface OnPause {
        void onPause();
    }

    public interface OnGetInfo {
        void onGetInfo(Song info);
    }

    public interface OnPlayModeChange {
        void OnPlayModeChange(PlayChangeMode mode);
    }

    public interface OnError {
        void onError(String err_msg);
    }

    public interface OnReset {
        void onReset();
    }

    public interface OnResume {
        void onResume(int playlist_pos);
    }

    public interface OnPlayFirstTime {
        void OnPlayFirstTime(int index);
    }

    public interface OnStop {
        void onStop(Cause cause, Object parma);

        enum Cause {
            Error,
            PlayFinish,
            Other
        }
    }

    public static class SongInfo {
        String Encode = "unknown encode", File_Path = "", Title = "<unknown title>", Artist = "<unknown artist>", Album = "<unknown album>", Url = "";
        Bitmap Cover = null;
        int Index = -1, Id = -1;
    }

    private class PlayHistoryRecoder {
        ArrayList<Integer> history = new ArrayList<Integer>();
        Random rand = new Random();
        Stack<Integer> mainStack = new Stack<Integer>();
        Stack<Integer> backupStack = new Stack<Integer>();
        int now = 0;

        void Reset(){
            history = new ArrayList<Integer>();
            rand = new Random();
            mainStack = new Stack<Integer>();
            backupStack = new Stack<Integer>();
            int now = 0;
        }

        void ResetRandomRecord(int max) {
            ArrayList<Integer> tmp = new ArrayList<Integer>();
            for (int i = 0; i < max; i++) {
                tmp.add(i);
            }
            history.clear();
            int t = -1;
            for (int i = max - 1; i >= 0; i++) {
                t = tmp.get(rand.nextInt(i));
                tmp.remove(Integer.valueOf(t));
                backupStack.push(t);
            }
        }

        int getPrev() {
            if (mainStack.size() == 0)
                return -1;
            backupStack.push(now);
            now = mainStack.pop();
            return now;
        }

        int getNext() {
            mainStack.push(now);
            if(play_list.size()==1)
                return 0;
            if (backupStack.size() == 0) {
                if (mode != PlayChangeMode.Shuffle) {
                    //It is Normal
                    if (now >= play_list.size() - 1) {
                        return -1;
                    } else {
                        now++;
                    }
                } else {
                    //It is Shuffle
                    now = rand.nextInt(play_list.size() - 1);
                }
            } else {
                now = backupStack.pop();
            }
            return now;
        }
    }

    class PreparedPlay implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer p1) {
            try {
                p1.start();
                if (!isPause) {
                    status = PlayerStatu.Playing;
                    if (trigger_onplay != null)
                        trigger_onplay.onPlay(index);
                } else {
                    p1.pause();
                    status = PlayerStatu.Pause;
                }
            } catch (Exception e) {
                e.fillInStackTrace();
                SendErrorMsg(e.getMessage());
                status = PlayerStatu.Error;
            }
        }
    }

    class CallerGetSongInfo implements Runnable {
        @Override
        public void run() {
            if (trigger_ongetinfo == null)
                return;
            trigger_ongetinfo.onGetInfo(getSongInfo(index));
        }
    }

    class TriggerPlayerError implements MediaPlayer.OnErrorListener {
        public boolean se(int p2, int code, String msg) {
            if (p2 == code) {
                SendErrorMsg(msg);
                return false;
            }
            return true;
        }

        @Override
        public boolean onError(MediaPlayer p1, int p2, int p3) {
            status = PlayerStatu.Error;
            if (se(p2, MediaPlayer.MEDIA_ERROR_IO, "io error"))
                if (se(p2, MediaPlayer.MEDIA_ERROR_SERVER_DIED, "server die"))
                    if (se(p2, MediaPlayer.MEDIA_ERROR_TIMED_OUT, "time out"))
                        if (se(p2, MediaPlayer.MEDIA_ERROR_UNKNOWN, "unknown error"))
                            if (se(p2, MediaPlayer.MEDIA_ERROR_UNSUPPORTED, "unsupport error")) {
                            }
            return true;
        }
    }

    class TriggerPlayerComplete implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer p1) {
            PlayNext();
        }
    }

}

