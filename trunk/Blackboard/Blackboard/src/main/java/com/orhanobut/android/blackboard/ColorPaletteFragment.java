package com.orhanobut.android.blackboard;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;

public class ColorPaletteFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    public static final String TAG = ColorPaletteFragment.class.getSimpleName();
    private OnFragmentInteractionListener listener;
    private GridView gridView;


    public static ColorPaletteFragment newInstance(){
        return new ColorPaletteFragment();
    }

    public ColorPaletteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View v=  inflater.inflate(R.layout.fragment_colors, container, false);

        gridView = (GridView) v.findViewById(R.id.color_list);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        gridView.setAdapter(new ColorPaletteAdapter(getActivity(), R.array.color_list));
        gridView.setOnItemClickListener(this);

        getDialog().getWindow()
                .getAttributes().windowAnimations = R.anim.abc_slide_in_bottom;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int color = (int) parent.getItemIdAtPosition(position);

        listener.onColorSelected(color);
        dismiss();
    }

    public interface OnFragmentInteractionListener {
        public void onColorSelected(int color);
    }

}
