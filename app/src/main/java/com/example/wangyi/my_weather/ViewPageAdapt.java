package com.example.wangyi.my_weather;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by wangyi on 2018/11/9.
 */
public class ViewPageAdapt extends PagerAdapter {
    private List<View> views;
    private Context context;

    public ViewPageAdapt(List<View> views, Context context){
        this.context = context;
        this.views = views;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position));
        return views.get(position);
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return (view == o);
    }
}

