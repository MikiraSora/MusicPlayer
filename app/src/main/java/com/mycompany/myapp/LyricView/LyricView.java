package com.mycompany.myapp.LyricView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by mikir on 2016/6/22.
 */
public class LyricView extends View {

    MediaPlayer player = null;
    ArrayList<LyricSentence> sentence = null;
    DisplayType displayType = DisplayType.FideOut;

    int color_highlight = Color.rgb(0, 255, 255);
    int color_normal = Color.rgb(255, 255, 255);

    int display_line = 5;

    OnRequestLyric onRequestLyric = null;

    public LyricView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        init();
    }

    public LyricView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricView(Context context) {
        this(context, null, 0);
    }

    private void init() {
    }

    public void setOnRequestLyric(OnRequestLyric func) {
        onRequestLyric = func;
    }

    public void setDisplayLine(int line) {
        display_line = line;
    }

    public void setDisplayColor(int highlight, int normal) {
        color_highlight = highlight;
        color_normal = normal;
    }

    public void bindMusicPlayer(MediaPlayer _player) {
        player = _player;
    }

    public void Pause() {
    }

    public void Start() {
    }

    public void Resume() {
    }

    public void Reset() {
    }

    public void SeekTo(long timeline) {
    }

    private void invaildUpdateDraw() {
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

    private String[] getLyric() {
        long timeline = player.getCurrentPosition();
        String[] lyric = new String[display_line];
        int pos = 0;
        while (pos != sentence.size()) {
            LyricSentence lyricSentence = sentence.get(pos);
            LyricSentence prevLyricSentence = getLyricSentence(pos - 1);
            if ((timeline >= prevLyricSentence.time) && (timeline <= lyricSentence.time)) {
                //curtten pos in sentences
                int _i = 0;
                for (int i = pos - display_line / 2; i < pos + display_line / 2; i++) {
                    lyric[_i] = getLyricSentence(i).content;
                    _i++;
                }
            }
            pos++;
        }
        return lyric;
    }

    public void setLyric(String text) {
        sentence = LyricDecoder.parseToSentence(text);
        if (onRequestLyric != null && sentence.size() == 0)
            sentence = onRequestLyric.onRequestLyric();
        if (sentence.size() == 0)
            sentence = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = canvas.getWidth(), height = canvas.getHeight();
        int perHeight = height / display_line;
        String[] lyric = getLyric();
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        switch (displayType) {
            case FideOut: {
                for (int i = 0; i < display_line; i++) {
                    //calculate alpha in cuttern line
                    if (i <= (display_line / 2)) {
                        paint.setAlpha(255 / (2 * display_line) * (i + 1));
                    } else if (i > display_line / 2) {
                        paint.setAlpha(255 / (2 * display_line) * (display_line - i));
                    }
                    paint.setARGB(paint.getAlpha(), Color.red(color_normal), Color.green(color_normal), Color.blue(color_normal));
                    if (i == display_line / 2) {
                        paint.setAlpha(255);
                    }
                    paint.setARGB(paint.getAlpha(), Color.red(color_highlight), Color.green(color_highlight), Color.blue(color_highlight));
                    canvas.drawText(lyric[i], 0, lyric[i].length(), width / 2, perHeight * i, paint);
                }
                break;
            }
        }


        //canvas.drawText();
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

    interface OnRequestLyric {
        ArrayList<LyricSentence> onRequestLyric();
    }
}
