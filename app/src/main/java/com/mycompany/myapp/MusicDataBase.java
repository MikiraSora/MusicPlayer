package com.mycompany.myapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by mikir on 2016/6/17.
 */

public class MusicDataBase extends SQLiteOpenHelper {

    public static String TAG = "MusicDatabase";

    private static String music_table_name = "musictable";
    private static String playlist_table_name = "playlisttable";
    private static String album_table_name = "albumtable";

    private static String database_name = "musicdatabase.db";

    private static int index_Title = 0, index_Artist = 1, index_Album = 2, index_File_Path = 3, index_AbsFile_path = 4;

    public MusicDataBase(Context context) {
        super(context, database_name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("create table if not exists " + music_table_name + "(Title,Artist,Album,File_Path,AbsFile_Path primary key)");
        database.execSQL("create table if not exists " + playlist_table_name + "(Name primary key,Files)");
        database.execSQL("create table if not exists " + album_table_name + "(Album primary key)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    public void Add(Song song) {
        ContentValues values = new ContentValues();
        values.put("Title", song.Title);
        values.put("Artist", song.Artist);
        values.put("Album", song.Album);
        values.put("AbsFile_Path", song.AbsFile_Path);
        this.getWritableDatabase().replace(music_table_name, "AbsFile_Path", values);
        RecordAlbum(song.Album);
        Log.i("sqlInfo", String.format("add song  :\"%s - %s\"", song.Title, song.Artist));
    }

    public void Add(Song[] songs) {
        for (Song song : songs)
            Add(song);
    }


    public Song GetSongsFromPlayList(String name) {
        Cursor cursor = getReadableDatabase().query(music_table_name, null, "File_Path=?", new String[]{name}, null, null, null, null);
        Song song;
        Log.i("sqlInfo", String.format("find playlist \"%s\" , found %d data", name, cursor.getCount()));
        while (cursor.moveToNext()) {
            song = new Song();
            song.AbsFile_Path = cursor.getString(index_AbsFile_path);
            song.Album = cursor.getString(index_Album);
            song.Artist = cursor.getString(index_Artist);
            song.Title = cursor.getString(index_Title);
            return song;
        }
        return null;
    }

    public boolean SavePlayList(String name, Song[] playlist) {
        if (getReadableDatabase().query(playlist_table_name, null, "Name=?", new String[]{name}, null, null, null, null).getCount() != 0)
            return false;

        JSONArray jsonArray = new JSONArray();
        for (Song song : playlist) {
            try {
                jsonArray.put(song.toJSON());
            } catch (Exception e) {
            }
        }
        String str = jsonArray.toString();

        Add(playlist);

        ContentValues cvalue = new ContentValues();
        cvalue.put("Name", name);
        cvalue.put("Files", str);

        getWritableDatabase().insert(playlist_table_name, "Name", cvalue);
        return true;
    }

    public boolean UpdatePlayList(String name, Song[] playlist) {
        if (getReadableDatabase().query(playlist_table_name, null, "Name=?", new String[]{name}, null, null, null, null).getCount() == 0)
            return false;
        JSONArray jsonArray = new JSONArray();
        for (Song song : playlist) {
            try {
                jsonArray.put(song.toJSON());
            } catch (Exception e) {
            }
        }
        String str = jsonArray.toString();

        ContentValues cvalue = new ContentValues();
        cvalue.put("Name", name);
        cvalue.put("Files", str);

        Add(playlist);

        try {
            getWritableDatabase().replace(playlist_table_name, "Name", cvalue);
        } catch (Exception e) {
            e.fillInStackTrace();
            Log.e(TAG, "cant update playlist " + name + (e.getCause()));
            return false;
        }
        return true;
    }

    public boolean DeletePlayList(String name) {
        if (getReadableDatabase().query(playlist_table_name, null, "Name=?", new String[]{name}, null, null, null, null).getCount() == 0)
            return false;
        getWritableDatabase().delete(playlist_table_name, "Name=?", new String[]{name});
        return true;
    }

    public Song[] LoadPlayList(String name)/*throws Exception*/ {
        Cursor cursor = getReadableDatabase().query(playlist_table_name, new String[]{"Name", "Files"}, "Name=?", new String[]{name}, null, null, null);
        if (cursor.getCount() == 0)
            return null;
        ArrayList<Song> list = new ArrayList<Song>();
        String str = null;
        JSONArray arr = null;
        JSONObject object;
        Song[] songs = null;
        Song song;

        while (cursor.moveToNext()) {
            try {
                str = cursor.getString(1);
                arr = new JSONArray(str);
                for (int i = 0; i < arr.length(); i++) {
                    String obj = arr.getString(i);
                    song = Song.parserJSON(obj);
                    list.add(song);
                }
            } catch (Exception e) {
                e.fillInStackTrace();
                Log.e(TAG, "cant load Song from playlist " + name + (e.getMessage()));
                return null;
            }
        }
        songs = new Song[list.size()];
        list.toArray(songs);

        SQLiteDatabase reader=getReadableDatabase();

        for(Song s:songs){
            cursor = getReadableDatabase().query(music_table_name,null, "File_Path=?", new String[]{s.AbsFile_Path}, null, null, null);
            if (cursor.getCount() == 0)
                continue;
            //s.AbsFile_Path = cursor.getString(index_AbsFile_path);
            s.Album = cursor.getString(index_Album);
            s.Artist = cursor.getString(index_Artist);
            s.Title = cursor.getString(index_Title);
            Log.i("MusicDataBase","load : "+s.toString());
        }

        return songs;
    }

    public String[] getAllPlayList() {
        Cursor cursor = getReadableDatabase().query(playlist_table_name, null, null, null, null, null, null);
        String[] result = new String[cursor.getCount()];
        ArrayList<String> arrlist = new ArrayList<String>();
        while (cursor.moveToNext()) {
            arrlist.add(cursor.getString(0));
        }
        arrlist.toArray(result);
        return result;
    }

    public Song getFirstSongOfPlayList(String name){
        Cursor cursor = getReadableDatabase().query(playlist_table_name, new String[]{"Name", "Files"}, "Name=?", new String[]{name}, null, null, null);
        if (cursor.getCount() == 0)
            return null;
        String str = null;
        JSONArray arr = null;
        JSONObject object;
        Song song;

        while (cursor.moveToNext()) {
            try {
                str = cursor.getString(1);
                arr = new JSONArray(str);
                for (int i = 0; i < 1; i++) {
                    String obj = arr.getString(i);
                    song=(Song.parserJSON(obj));
                    Log.i("MusicDataBase","load the first song of playlist : "+song.toString());
                    return song;
                }
            } catch (Exception e) {
                e.fillInStackTrace();
                Log.e(TAG, "cant load Song from playlist " + name + (e.getMessage()));
                return null;
            }
        }
        return null;
    }

    public void RecordAlbum(String name){
        SQLiteDatabase database=getReadableDatabase();
        Cursor cursor=database.query(album_table_name,null,"Album=?",new String[]{name},null,null,null,null);
        if(cursor.getCount()==0){
            ContentValues values=new ContentValues();
            values.put("Album",name);
            getWritableDatabase().insert(album_table_name,null,values);
        }else{
            //？
        }
    }

    public String[] getAllAlbum(){
        Cursor cursor=getReadableDatabase().query(album_table_name,null,"Album=?",null,null,null,null,null);
        if(cursor.getCount()!=0) {
            String[] names = new String[cursor.getCount()];
            int i=0;
            while(cursor.moveToNext()){
                names[i]=cursor.getString(0);
                i++;
            }
            return names;
        }
        return null;
    }

    public Song getFirstSongOfAlbum(String name){
        Cursor cursor = getReadableDatabase().query(music_table_name, null, "Album=?", new String[]{name}, null, null, null, null);
        Song song;
        Log.i("sqlInfo", String.format("find album \"%s\" , found %d data", name, cursor.getCount()));
        while (cursor.moveToNext()) {
            song = new Song();
            song.AbsFile_Path = cursor.getString(index_AbsFile_path);
            song.Album = cursor.getString(index_Album);
            song.Artist = cursor.getString(index_Artist);
            song.Title = cursor.getString(index_Title);
            return song;
        }
        return null;
    }
}
