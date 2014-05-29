package com.orhanobut.android.blackboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * Created by limon on 25.5.2014.
 */
public class ColorPaletteAdapter extends BaseAdapter {

    private final LayoutInflater layoutInflater;
    private final int[] list;

    public ColorPaletteAdapter(Context context, int list) {
        layoutInflater = LayoutInflater.from(context);
        this.list = context.getResources().getIntArray(list);
    }

    @Override
    public int getCount() {
        return list.length;
    }

    @Override
    public Object getItem(int i) {
        return list[i];
    }

    @Override
    public long getItemId(int i) {
        return list[i];
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imageView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.color_palette_item, viewGroup, false);
        }

        imageView = (ImageView) view.findViewById(R.id.color_palette);
        imageView.setBackgroundColor(list[i]);

        return view;
    }
}
