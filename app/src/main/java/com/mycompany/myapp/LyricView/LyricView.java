package com.mycompany.myapp.LyricView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mycompany.myapp.ExMusicPlayer;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

/**
 * Created by mikir on 2016/6/22.
 */
public class LyricView extends View {

    ExMusicPlayer player = null;
    ArrayList<LyricSentence> sentence = null;
    DisplayType displayType = DisplayType.FideOut;

    Status status = Status.Ready;

    Handler handler = new Handler();
    DrawingLooper drawingLooper = new DrawingLooper();

    int update_inv = 100;

    float prev_x = 0, prev_y = 0;

    long time_offset = 0;

    float rec_x, rec_y;

    boolean ableDisplay = true;

    float scroll_shift = 107.5f;
    long scroll_time = 0;
    long scroll_init_time = 0;
    long scroll_hint = 500;
    OnScrollFinished trigger_scrollfinish = null;
    OnScrolling trigger_scrolling = null;

    OnRequestShowOrHide trigger_requestShowOrHide = null;

    int color_highlight = Color.rgb(0, 0, 255);
    int color_normal = Color.rgb(0, 0, 255);
    int color_hint = Color.rgb(255, 0, 0);

    boolean isScrolling = false;

    int display_line = 14;

    OnRequestLyric onRequestLyric = null;

    public LyricView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
    }

    public LyricView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricView(Context context) {
        this(context, null, 0);
    }

    public void setOnRequestLyric(OnRequestLyric func) {
        onRequestLyric = func;
    }

    public void setDisplayLine(int line) {
        display_line = line;
    }

    public void setDisplayColor(int highlight, int normal, int hint) {
        color_highlight = highlight;
        color_normal = normal;
        color_hint = hint;
    }

    public long getDisplayOffset() {
        return time_offset;
    }

    public void setDisplayOffset(long offset) {
        time_offset = offset;
    }

    public void setOnScrolling(OnScrolling s) {
        trigger_scrolling = s;
    }

    public void setOnRequestShowOrHide(OnRequestShowOrHide e) {
        trigger_requestShowOrHide = e;
    }

    public void setOnScrollFhinished(OnScrollFinished s) {
        trigger_scrollfinish = s;
    }

    public void bindMusicPlayer(ExMusicPlayer _player) {
        player = _player;
    }

    public void Pause() {
        handler.removeCallbacks(drawingLooper);
    }

    public void Start() {
        handler.postDelayed(drawingLooper, update_inv);
    }

    public void Resume() {
        handler.postDelayed(drawingLooper, update_inv);
    }

    public void Reset() {
        handler.removeCallbacks(drawingLooper);
    }

    public void SeekTo(long timeline) {

    }


    private boolean Check() {
        if (player == null)
            return false;
        if (ableDisplay == false)
            return false;
        return sentence != null;
    }

    private LyricSentence getLyricSentence(int pos) {
        if (pos >= 0) {
            if (pos < sentence.size())
                return sentence.get(pos);
        }
        LyricSentence tmp = new LyricSentence();
        tmp.content = "";
        if (pos < 0)
            tmp.time = 0;
        else
            tmp.time = Long.MAX_VALUE;
        return tmp;
    }

    private LyricSentence[] getLyric() {
        long timeline = isScrolling ? getSrollTime() : player.getCurrentTimeNow();
        boolean tmpFix = false;

        if (timeline >= (sentence.get(sentence.size() - 1).time)) {
            timeline = (sentence.get(sentence.size() - 1).time);
            tmpFix = true;
        }

        LyricSentence[] lyric = new LyricSentence[display_line];
        int pos = 0;
        while (pos != sentence.size()) {
            LyricSentence lyricSentence = sentence.get(pos);
            LyricSentence prevLyricSentence = getLyricSentence(pos - 1);
            if ((timeline >= prevLyricSentence.time) && (timeline <= lyricSentence.time)) {
                //curtten pos in sentences
                int base_pos = pos - (display_line / 2) - 1;
                for (int i = 0; i < display_line; i++) {
                    lyric[i] = getLyricSentence(base_pos + i);
                }
                break;
            }
            pos++;
        }
        return lyric;
    }

    public void setLyric(String text) {
        sentence = LyricDecoder.parseToSentence(text);
        LyricDecoder.rearrangeLyricSentence(sentence);
        if (onRequestLyric != null && sentence.size() == 0)
            sentence = onRequestLyric.onRequestLyric();
        if (sentence.size() == 0)
            sentence = null;
    }

    public void setLyricFromFile(String abs_path) {
        try {
            Reader reader = new InputStreamReader(new FileInputStream(abs_path));
            int c = -1;
            String str = new String();
            while ((c = reader.read()) != -1) {
                str += (char) c;
            }
            setLyric(str);
        } catch (Exception e) {
            Log.e("LyricView", e.getMessage());
            sentence = null;
        }
    }

    public void Hide() {
        ableDisplay = false;
    }

    public void Show() {
        ableDisplay = true;
    }

    public boolean isHide() {
        return !ableDisplay;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!Check())
            return;

        int width = canvas.getWidth(), height = canvas.getHeight();
        int perHeight = height / display_line;
        LyricSentence[] lyric = getLyric();
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setTextSize(45);
        int center_line = display_line / 2;
        int extra_line = 0;
        int fix_pos = 0;
        switch (displayType) {
            case FideOut: {
                for (int i = 0; i < display_line; i++) {
                    if (i <= (display_line / 2)) {
                        paint.setAlpha(255 / (2 * display_line) * (i + 1));
                    } else if (i > display_line / 2) {
                        paint.setAlpha(255 / (2 * display_line) * (display_line - i));
                    }
                    paint.setARGB(paint.getAlpha(), Color.red(color_normal), Color.green(color_normal), Color.blue(color_normal));
                    if (lyric[i] == null)
                        continue;

                    if (lyric[i].time == lyric[center_line].time) {
                        paint.setAlpha(255);
                        paint.setARGB(paint.getAlpha(), Color.red(color_highlight), Color.green(color_highlight), Color.blue(color_highlight));
                        fix_pos++;
                    }

                    if (lyric[i].content.length() == 0)
                        lyric[i].content = new String();

                    extra_line = DrawText(lyric[i].content, canvas, paint, i, extra_line);
                }
                break;
            }
        }
        /*
        滑动划线
         */
        Paint scroll_paint = new Paint();
        scroll_paint.setAntiAlias(true);
        scroll_paint.setTextSize(45);
        scroll_paint.setColor(color_hint);
        if (!isScrolling)
            return;
        int sX = 0, sY = canvas.getHeight() / 2, eX = canvas.getWidth(), eY = sY;
        canvas.drawLine(sX, sY, eX, eY, scroll_paint);
        long nowTime = getSrollTime();
        String timeStr = coverTimeToString(nowTime);
        canvas.drawText(timeStr, 0, timeStr.length(), sX, sY, scroll_paint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean res = super.onTouchEvent(event);
        /*
        if (!Check()){
            if(!ableDisplay)
                if(trigger_requestShowOrHide!=null)
                    trigger_requestShowOrHide.onRequestShowOrHide();
            return false;
        }*/

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                prev_x = event.getX();
                prev_y = event.getY();
                isScrolling = true;
                scroll_time = player.getCurrentTimeNow();
                scroll_init_time = player.getCurrentTimeNow();
                rec_x = event.getX();
                rec_y = event.getY();
                break;

            case MotionEvent.ACTION_UP:
                isScrolling = false;
                if (Math.abs(getSrollTime() - scroll_init_time) > scroll_hint) {
                    if (!Check())
                        break;
                    if (trigger_scrollfinish != null)
                        trigger_scrollfinish.onScrolliFinished((int) getSrollTime());
                }
                float _x = event.getX(), _y = event.getY();
                if (_x == rec_x && _y == rec_y) {
                    if (trigger_requestShowOrHide != null)
                        trigger_requestShowOrHide.onRequestShowOrHide();
                }
                break;


            case MotionEvent.ACTION_MOVE:
                if (!isScrolling)
                    break;
                if (!Check())
                    break;
                float x = event.getX(), y = event.getY();
                if (x == prev_x && y == prev_y)
                    break;
                float inv_y = y - prev_y;
                ScrollLyric(-inv_y);
                prev_x = event.getX();
                prev_y = event.getY();
                break;


        }
        return true;
    }

    private void ScrollLyric(float size) {
        long time = getSrollTime();
        time += size * scroll_shift;
        if (time < 0)
            time = 0;
        scroll_time = time;
    }

    private String coverTimeToString(long time) {
        long min, sec, msec;

        min = time / (60000);
        sec = (time - min * 60000) / 1000;
        msec = time - (sec * 1000) - (min * 60000);

        return String.format("%02d:%02d.%02d", min, sec, msec);
    }

    private long getSrollTime() {
        return scroll_time;
    }


    public int DrawText(String text, Canvas canvas, Paint paint, int time, int extra_line) {
        char[] chars = text.toCharArray();
        int perHeight = canvas.getHeight() / display_line;
        int cut_count = paint.breakText(chars, 0, chars.length, canvas.getWidth(), null);
        String lyric = text.substring(0, cut_count);
        canvas.drawText(lyric, 0, lyric.length(), canvas.getWidth() / 2, perHeight * (time + extra_line), paint);
        String nextStr = text.substring(cut_count, text.length());
        if (nextStr.length() == 0)
            return extra_line;
        return DrawText(nextStr, canvas, paint, time, extra_line + 1);
    }
    /*
    Inner Class/Enum define.
     */
    enum Status {
        Playing,
        Pause,
        Ready,
        Stop,
        Error
    }

    enum DisplayType {
        FideOut,
        NonFide,
        Highlight
    }

    public interface OnRequestLyric {
        ArrayList<LyricSentence> onRequestLyric();
    }

    public interface OnScrolling {
        void onScrolling(int vspeed);
    }

    public interface OnScrollFinished {
        void onScrolliFinished(int time);
    }

    public interface OnRequestShowOrHide {
        void onRequestShowOrHide();
    }

    class DrawingLooper implements Runnable {
        @Override
        public void run() {
            postInvalidate();
            postDelayed(this, update_inv);
        }
    }
}
