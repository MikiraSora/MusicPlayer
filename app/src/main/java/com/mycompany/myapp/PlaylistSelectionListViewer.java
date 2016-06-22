package com.mycompany.myapp;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mikir on 2016/6/19.
 */
public class PlaylistSelectionListViewer extends ListView {
    Context ctx;
    int selected_id = -1;
    PlaylistAdapter playlistAdapter = new PlaylistAdapter();

    PlaylistSelectionListViewer(Context _ctx) {
        super(_ctx);
        ctx = _ctx;
        setAdapter(playlistAdapter);
    }

    String getResult() {
        return selected_id < 0 ? null : playlistAdapter.playlistname_list.get(selected_id);
    }

    void addPlayListName(String name) {
        playlistAdapter.addPlaylist(name);
    }

    class PlaylistAdapter extends BaseAdapter {
        ArrayList<String> playlistname_list = new ArrayList<String>();

        TextView last_selected = null;

        void addPlaylist(String name) {
            playlistname_list.add(name);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return playlistname_list.size();
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public Object getItem(int i) {
            return playlistname_list.get(i);
        }

        @Override
        public View getView(int id, View view, ViewGroup viewGroup) {
            final TextView textView;
            if (view == null) {
                textView = new TextView(ctx);
            } else {
                textView = (TextView) view;
            }

            textView.setText((String) getItem(id));
            //textView.setBackgroundColor(Color.rgb(255,255,255));
            textView.setHeight(80);
            textView.setTextSize(15.0f);
            if (id == selected_id) {
                textView.setBackgroundColor(Color.rgb(0, 0, 255));
                textView.setTextColor(Color.rgb(255, 255, 255));
            } else {
                textView.setBackgroundColor(Color.rgb(255, 255, 255));
                textView.setTextColor(Color.rgb(0, 0, 0));
            }
            textView.setTag(id);
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected_id = (int) view.getTag();
                    view.setBackgroundColor(Color.rgb(0, 0, 255));
                    ((TextView) view).setTextColor(Color.rgb(255, 255, 255));
                    if (last_selected != null && last_selected != view) {
                        last_selected.setBackgroundColor(Color.rgb(255, 255, 255));
                        last_selected.setTextColor(Color.rgb(0, 0, 0));
                    }
                    last_selected = (TextView) view;
                }
            });

            return textView;
        }
    }
}
