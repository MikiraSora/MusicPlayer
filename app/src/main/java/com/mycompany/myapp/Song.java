package com.mycompany.myapp;

import org.json.JSONObject;

/**
 * Created by mikir on 2016/6/19.
 */
public class Song {
    public String Title, Artist, AbsFile_Path, Album;

    public static Song parserJSON(String json_str) throws Exception {
        Song song = new Song();
        JSONObject jsonObject = new JSONObject(json_str);

        song.Title = jsonObject.getString("Title");
        song.AbsFile_Path = jsonObject.getString("AbsFile_Path");
        song.Album = jsonObject.getString("Album");
        song.Artist = jsonObject.getString("Artist");

        return song;
    }

    public String toJSON() throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("AbsFile_Path", AbsFile_Path);
        jsonObject.put("Album", Album);
        jsonObject.put("Artist", Artist);
        jsonObject.put("Title", Title);

        return jsonObject.toString();
    }
}
