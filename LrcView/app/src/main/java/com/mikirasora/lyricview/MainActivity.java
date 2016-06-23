package com.mikirasora.lyricview;

import android.app.*;
import android.os.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		try{
			
		InputStreamReader r=new InputStreamReader(new FileInputStream("/sdcard/netease/美郷あき - Glitter.lrc"));
		String str=new String();
		int i=-1;
		while((i=r.read())!=-1)
			str+=(char)i;
		ArrayList<LyricSentence> list= LyricDecoder.parseToSentence(str);
		System.out.println();
		}catch(Exception e){
			e.fillInStackTrace();
		}
    }
}
