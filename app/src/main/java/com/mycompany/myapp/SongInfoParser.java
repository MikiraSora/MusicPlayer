package com.mycompany.myapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.mpatric.mp3agic.Mp3File;

public class SongInfoParser {
    Mp3File file;

    private SongInfoParser() {
    }

    public SongInfoParser(String _input) {
        try {
            file = new Mp3File(_input);
        } catch (Exception e) {
            Log.e("SongInfoParser", e.getMessage());
        }
    }

    public String getTitle() {
        if (file == null)
            return "unknown";
        String val = "unknown";
        if (file.hasId3v2Tag()) {
            val = file.getId3v2Tag().getTitle();
            if (val == null)
                return "unknown";
            if (val.length() != 0)
                return val;
        }
        if (file.hasId3v1Tag()) {
            val = file.getId3v1Tag().getTitle();
            if (val.length() != 0)
                return val;
        }

        return val;
    }

    public String getArtist() {
        if (file == null)
            return "unknown";
        String val = "unknown";
        if (file.hasId3v2Tag()) {
            val = file.getId3v2Tag().getArtist();
            if (val == null)
                return "unknown";
            if (val.length() != 0)
                return val;
            val = file.getId3v2Tag().getAlbumArtist();
        }
        if (file.hasId3v1Tag()) {
            val = file.getId3v1Tag().getArtist();
            if (val.length() != 0)
                return val;
        }

        return val;
    }

    public String getAlbum() {
        if (file == null)
            return "unknown";
        String val = "unknown";
        if (file.hasId3v2Tag()) {
            val = file.getId3v2Tag().getAlbum();
            if (val == null)
                return "unknown";
            if (val.length() != 0)
                return val;
        }
        if (file.hasId3v1Tag()) {
            val = file.getId3v1Tag().getAlbum();
            if (val.length() != 0)
                return val;
        }

        return val;
    }

    public Bitmap getCover() {
        if (file == null)
            return null;
        if (file.hasId3v2Tag()) {
            try {
                byte[] data = file.getId3v2Tag().getAlbumImage();
                return BitmapFactory.decodeByteArray(data, 0, data.length);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public String getEncode() {
        try {
            return file.getId3v2Tag().getEncoder();
        } catch (Exception e) {
        }
        return "unknown emcode";
    }
}
