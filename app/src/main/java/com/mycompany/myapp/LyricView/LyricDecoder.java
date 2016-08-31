package com.mycompany.myapp.LyricView;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricDecoder {
    private static final Pattern raw_reg = Pattern.compile("\\[\\d{2}\\d*\\:\\d{2}(\\.\\d*)?\\]*.*"), split_lrc_reg = Pattern.compile("(\\[\\d{2}\\d*\\:\\d{2}(\\.\\d*)?\\])(.*)"), reg_time_reg = Pattern.compile("\\[(\\d{2}\\d*)\\:(\\d{2})(\\.(\\d*))?\\].*");
    private static final int time_min = 1, time_sec = 2, time_msec = 4, lrc = 3;

    static ArrayList<String> parseToString(String source_text) {
        ArrayList<String> list = new ArrayList<String>();
        Matcher res = raw_reg.matcher(source_text);
        String str = null;

        while (res.find()) {
            str = res.group();
            str = FixString(str);
            list.add(str);
        }

        return list;
    }

    static LyricSentence parseOneToSentence(String str) {
        return parseToSentence(str).get(0);
    }

    private static String FixString(String input) {
        String output = input;

        //if has "\r" in the end of string
        int pos = output.indexOf("\\r");
        if (pos == (output.length() - 2))
            output = output.substring(0, output.length() - 2);

        return output;
    }

    static ArrayList<LyricSentence> parseToSentence(String source_text) {
        ArrayList<LyricSentence> list = new ArrayList<LyricSentence>();
        ArrayList<String> rawLrc = parseToString(source_text);
        Matcher res = null;
        LyricSentence sentence = null;

        for (String text : rawLrc) {
            res = split_lrc_reg.matcher(text);
            if (!res.matches())
                continue;
            sentence = new LyricSentence();
            sentence.content = res.group(lrc);
            res = reg_time_reg.matcher(text);
            if (!res.matches())
                continue;
            sentence.time = ConverToMs(Integer.parseInt(res.group(time_min)), Integer.parseInt(res.group(time_sec)), Integer.parseInt(res.group(time_msec)));

            list.add(sentence);
        }
        return list;
    }

    private static long ConverToMs(int min, int sec, int msec) {
        return min * 60000 + sec * 1000 + msec;
    }

    public static ArrayList<LyricSentence> rearrangeLyricSentence(ArrayList<LyricSentence> lyricSentences) {
        Collections.sort(lyricSentences, new Comparator<LyricSentence>() {
            @Override
            public int compare(LyricSentence lyricSentence, LyricSentence t1) {
                return (int) (lyricSentence.time - t1.time);
            }
        });
        for (LyricSentence sentence : lyricSentences)
            Log.d("LyricDecoder", sentence.toString());
        return lyricSentences;
    }
}
