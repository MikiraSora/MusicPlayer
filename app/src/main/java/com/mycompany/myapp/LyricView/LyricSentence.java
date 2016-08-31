package com.mycompany.myapp.LyricView;

public class LyricSentence {
    long time = -1;
    String content = "";

    @Override
    public String toString() {
        long min, sec, msec;

        min = time / (60000);
        sec = (time - min * 60000) / 1000;
        msec = time - (sec * 1000) - (min * 60000);

        return String.format("[%02d:%02d.%02d]%s", min, sec, msec, content);
    }
}
