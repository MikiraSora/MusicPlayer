package com.mikirasora.lyricview;
import android.view.*;
import android.media.*;
import android.graphics.*;
import android.content.*;
import android.util.*;
import java.util.*;
import android.widget.*;

public class LyricView extends View
{
	private MediaPlayer player=null;
	private ArrayList<LyricSentence> lrc_list=null;
	private boolean ableDump=false;
	private Context ctx;
	private LyricView.Status statu=LyricView.Status.None;
	private LyriListView listview=null;
	
	private LyricView(){super(null,null);}
	public LyricView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs);
		ctx=context;
		init();
	}
	public LyricView(Context context, AttributeSet attrs){this(context, attrs, 0);}
	public LyricView(Context context){this(context, null, 0);}
	
	protected void init(){}
	
	public void setPlayerListener(MediaPlayer _player){player=_player;}
	public MediaPlayer getPlayerListener(){return player;}
	
	public void setLyric(ArrayList<LyricSentence> _list){lrc_list=_list;}
	public void setLyric(String raw_text){lrc_list=LyricDecoder.parseToSentence(raw_text);}
	public void setLyric(String[] _list){
		lrc_list=new ArrayList<LyricSentence>();
		LyricSentence sentence=null;
		for(String _s:_list){
			if((sentence=LyricDecoder.parseOneToSentence(_s))!=null)
				lrc_list.add(sentence);
			sentence=null;
		}
	}
	
	public void setAdapter(BaseAdapter _adapter){}
	
	public void enableDump(boolean _switch){ableDump=_switch;}
	
	public ArrayList getLyric(){return lrc_list;}
	
	public void callUpdateImmediately(){
		if(player==null)
			return;
		this.OnUpdate(player.getCurrentPosition());
	}
	
	public void callDrawImmediately(){
		this.invalidate();
	}
	
	private void sendErrorMassage(String msg){
		Log.e("LyricView_Error",msg);
		if(ableDump)
			Toast.makeText(ctx,msg,Toast.LENGTH_SHORT).show();
	}
	
	private boolean checkAll(){
		if(player==null){
			sendErrorMassage("havent call setPlayer(...) and loaded MediaPlayer.");
			return false;
		}
		if(listview==null){
			sendErrorMassage("havent call setListView(...) and loaded LyricListView.");
			return false;
		}
		if(lrc_list==null){
			sendErrorMassage("havent call setLyric(...) and loaded LyricSentence/String[]/String");
			return false;
		}
		return true;
	}
	
	
	public void OnUpdate(long timeline){
		if(!checkAll())
			return;
		
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
	}
	
	public static enum Status{
		Update,
		Pause,
		Error,
		Ready,
		None
		}
	
}
