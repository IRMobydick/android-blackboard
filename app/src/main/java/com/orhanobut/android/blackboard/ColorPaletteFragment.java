package com.orhanobut.android.blackboard;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ColorPaletteFragment extends DialogFragment implements
        AdapterView.OnItemClickListener {

    public static final String TAG = ColorPaletteFragment.class.getSimpleName();

    private OnFragmentInteractionListener listener;

    @InjectView(R.id.color_list) GridView gridView;

    public static ColorPaletteFragment newInstance() {
        return new ColorPaletteFragment();
    }

    public ColorPaletteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
        View view = inflater.inflate(R.layout.fragment_colors, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int[] colorList = getActivity().getResources().getIntArray(R.array.color_list);
        gridView.setAdapter(new ColorPaletteAdapter(getActivity(), colorList));
        gridView.setOnItemClickListener(this);

        getDialog().getWindow().getAttributes().windowAnimations = R.anim.abc_slide_in_bottom;
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
        listener = null;
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
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
