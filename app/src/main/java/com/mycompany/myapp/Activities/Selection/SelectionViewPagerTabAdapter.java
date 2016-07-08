package com.mycompany.myapp.Activities.Selection;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by mikir on 2016/7/6.
 */
public class SelectionViewPagerTabAdapter extends PagerAdapter{
    ArrayList<TabViewItem> containter=new ArrayList<>();

    TabViewItem add(TabViewItem item){
        containter.add(item);
        notifyDataSetChanged();
        return item;
    }

    boolean remove(TabViewItem item){return containter.remove(item);}


    //viewpager中的组件数量
    @Override
    public int getCount() {
        return containter.size();
    }
    //滑动切换的时候销毁当前的组件
    @Override
    public void destroyItem(ViewGroup container, int position,
                            Object object) {
        ((ViewPager) container).removeView(containter.get(position).view);
    }
    //每次滑动的时候生成的组件
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ((ViewPager) container).addView(containter.get(position).view);
        return containter.get(position).view;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return containter.get(position).title;
    }
    public static class TabViewItem{
        View view;
        String title;
    }
}
