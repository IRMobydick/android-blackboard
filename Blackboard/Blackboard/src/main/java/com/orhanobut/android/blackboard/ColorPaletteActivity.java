package com.orhanobut.android.blackboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ColorPaletteActivity extends Activity implements AdapterView.OnItemClickListener{

    private static final String COLOR_ID = "COLOR_ID";
    private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_color_palette);

        mGridView = (GridView) findViewById(R.id.gridViewColorPalette);
        mGridView.setAdapter(new ColorPaletteAdapter(this, R.array.color_list));
        mGridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent();
        intent.putExtra(COLOR_ID,adapterView.getItemIdAtPosition(i));
        setResult(RESULT_OK, intent);
        finish();
    }

    class ColorPaletteAdapter extends BaseAdapter {

        private final LayoutInflater mLayoutInflater;
        private final int[] mColorList;

        public ColorPaletteAdapter(Context context, int list) {
            mLayoutInflater = LayoutInflater.from(context);
            mColorList = context.getResources().getIntArray(list);
        }

        @Override
        public int getCount() {
            return mColorList.length;
        }

        @Override
        public Object getItem(int i) {
            return mColorList[i];
        }

        @Override
        public long getItemId(int i) {
            return mColorList[i];
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ImageView imageView = null;
            if (view == null) {
                view = mLayoutInflater.inflate(R.layout.color_palette_item, viewGroup, false);
            }

            imageView = (ImageView) view.findViewById(R.id.buttonColorPalette);
            imageView.setBackgroundColor(mColorList[i]);

            return view;
        }
    }
}
