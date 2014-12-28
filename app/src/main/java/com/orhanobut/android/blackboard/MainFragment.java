package com.orhanobut.android.blackboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * @author Orhan Obut
 */
public class MainFragment extends Fragment implements
        DrawView.DrawListener {

    private static final String TAG = MainFragment.class.getSimpleName();
    private static final String KEY_NAME = "name";
    private static final int SHARE_REQUEST_CODE = 10;
    private static final String IMAGE_TEMP_PATH = Environment.getExternalStorageDirectory().toString() + "/blackboard";

    private DrawView drawView;
    private boolean isEraserActive;
    private View rootView;
    private OnFragmentListener listener;

    @InjectView(R.id.replay) Button buttonPlay;
    @InjectView(R.id.button_container) View buttonContainer;
    @InjectView(R.id.draw_button_container) View drawButtonContainer;
    @InjectView(R.id.author) TextView textViewAuthor;
    @InjectView(R.id.exit_replay_button) Button exitReplayButton;

    public static Fragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity + " should implement OnFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, view);
        drawView = new DrawView(getActivity(), this);

        if (savedInstanceState == null) {

        }

        FrameLayout drawContainer = (FrameLayout) view.findViewById(R.id.draw_layout);
        drawContainer.addView(drawView);

        rootView = getActivity().getWindow().getDecorView().getRootView();
        buttonContainer.bringToFront();
        textViewAuthor.setText(getName());

        return view;
    }

    private void replay(View view) {
        if (!drawView.isReplaying()) {
            drawButtonContainer.setVisibility(View.GONE);
            view.setBackgroundResource(R.drawable.pause);
        } else {
            view.setBackgroundResource(R.drawable.replay);
        }

        exitReplayButton.setVisibility(View.VISIBLE);
        drawView.replay();
    }

    private void reset() {
        drawView.reset();
        listener.onResetClicked();
    }

    private void setEraser(View view) {
        if (isEraserActive) {
            drawView.setPen();
            view.setBackgroundResource(R.drawable.clean);
        } else {
            drawView.setEraser();
            view.setBackgroundResource(R.drawable.pencil);
        }
        isEraserActive = !isEraserActive;
    }

    private void selectPenColor() {
        ColorPaletteFragment.newInstance().show(getFragmentManager(), ColorPaletteFragment.TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        drawView.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        drawView.onPause();
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.share)
    void onShareClick() {
        share();
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.reset)
    void onResetClick() {
        reset();
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.replay)
    void onReplayClick(View view) {
        replay(view);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.eraser)
    void onEraserClick(View view) {
        setEraser(view);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.color_palette)
    void onColorPaletteClick() {
        selectPenColor();
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.author)
    void onAuthorClick(View view) {
        changeName(view);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.exit_replay_button)
    void onExitReplay(View view) {
        drawView.exitReplay();
    }

    private void changeName(View v) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setMessage(getString(R.string.enter_your_name));

        final EditText input = new EditText(getActivity());
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String text = input.getText().toString();
                textViewAuthor.setText(text);
                saveName(text);
            }
        });

        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private void saveName(String value) {
        final SharedPreferences pref = getActivity().getSharedPreferences("blackboard", Activity.MODE_PRIVATE);
        pref.edit().putString(KEY_NAME, value).commit();
    }

    private String getName() {
        final SharedPreferences pref = getActivity().getSharedPreferences("blackboard", Activity.MODE_PRIVATE);
        final String value = pref.getString(KEY_NAME, getString(R.string.click_change_name));

        return (value.isEmpty() ? getString(R.string.click_change_name) : value);
    }

    private void share() {
        Uri uri = takeScreenShot();

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivityForResult(Intent.createChooser(share, getString(R.string.share)), SHARE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHARE_REQUEST_CODE) {
            deleteAllFiles(IMAGE_TEMP_PATH);
        }
    }

    private void deleteAllFiles(String path) {
        final File dir = new File(path);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                new File(dir, aChildren).delete();
            }
        }
    }

    public Uri takeScreenShot() {
        buttonContainer.setVisibility(View.GONE);

        final Time time = new Time();
        time.setToNow();

        rootView.setDrawingCacheEnabled(true);

        final File folder = new File(IMAGE_TEMP_PATH);
        if (!folder.exists()) {
            folder.mkdir();
        }

        final String path = folder + "/" + time.format("%Y%M%d%k%M%S") + ".jpeg";

        Bitmap drawingCache = rootView.getDrawingCache();
        Bitmap drawBitmap = drawView.getBitmap();
        if (drawBitmap.isRecycled()) {
            buttonContainer.setVisibility(View.VISIBLE);
            return null;
        }
        Bitmap result = overlay(drawingCache, drawBitmap);

        rootView.setDrawingCacheEnabled(false);

        OutputStream outputStream;
        final File imageFile = new File(path);

        try {
            outputStream = new FileOutputStream(imageFile);
            result.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (FileNotFoundException e) {
            Log.d(TAG, e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        } finally {
            result.recycle();
            drawingCache.recycle();
            rootView.destroyDrawingCache();
            result = null;
        }

        buttonContainer.setVisibility(View.VISIBLE);

        return Uri.fromFile(new File(path));
    }

    private Bitmap overlay(Bitmap backBitmap, Bitmap topBitmap) {
        Bitmap overlayBitmap = Bitmap.createBitmap(backBitmap.getWidth(), backBitmap.getHeight(), backBitmap.getConfig());
        Canvas canvas = new Canvas(overlayBitmap);
        canvas.drawBitmap(backBitmap, new Matrix(), null);

        int padding = (int) getResources().getDimension(R.dimen.draw_view_padding);

        canvas.drawBitmap(topBitmap, padding, padding, null);

        return overlayBitmap;
    }

    @Override
    public void onReplayCompleted() {
        Log.d(TAG, "onReplayCompleted");
        if (!isVisible()) {
            Log.d(TAG, "onReplayCompleted : is not visible");
            return;
        }
        drawButtonContainer.setVisibility(View.VISIBLE);
        buttonPlay.setBackgroundResource(R.drawable.replay);
        exitReplayButton.setVisibility(View.GONE);
    }

    @Override
    public void onPaused() {
        buttonPlay.setBackgroundResource(R.drawable.replay);
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        drawView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onPlaying() {
        buttonPlay.setBackgroundResource(R.drawable.pause);
    }

    @SuppressWarnings("UnusedDeclared")
    public void onEvent(ColorChangedEvent event) {
        drawView.setPaintColor(event.getColor());
    }

    public interface OnFragmentListener {

        public void onResetClicked();
    }
}
