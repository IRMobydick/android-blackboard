package com.orhanobut.android.blackboard;import android.app.AlertDialog;import android.content.DialogInterface;import android.content.Intent;import android.content.SharedPreferences;import android.graphics.Bitmap;import android.graphics.Canvas;import android.graphics.Matrix;import android.net.Uri;import android.os.Bundle;import android.os.Environment;import android.support.v4.app.FragmentActivity;import android.text.format.Time;import android.view.MotionEvent;import android.view.View;import android.view.animation.Animation;import android.view.animation.AnimationUtils;import android.widget.Button;import android.widget.EditText;import android.widget.FrameLayout;import android.widget.TableRow;import android.widget.TextView;import java.io.File;import java.io.FileNotFoundException;import java.io.FileOutputStream;import java.io.IOException;import java.io.OutputStream;public class MainActivity extends FragmentActivity implements        View.OnClickListener,        DrawView.DrawListener,        ColorPaletteFragment.OnFragmentInteractionListener {    private static final String KEY_NAME = "name";    private static final int SHARE_REQUEST_CODE = 10;    private static final String IMAGE_TEMP_PATH = Environment.getExternalStorageDirectory().toString() + "/blackboard";    private DrawView drawView;    private boolean isEraserActive;    private View rootView;    private Animation animClick;    private View buttonsLayout;    private View drawButtonsLayout;    private TextView textViewAuthor;    private Button buttonPlay;    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.activity_main);        drawView = new DrawView(this, this);        FrameLayout v = (FrameLayout) findViewById(R.id.draw_layout);        v.addView(drawView);        rootView = getWindow().getDecorView().getRootView();        animClick = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);        findViewById(R.id.buttons_layout).bringToFront();        findViewById(R.id.screenshot).setOnClickListener(this);        buttonPlay = (Button) findViewById(R.id.replay);        buttonPlay.setOnClickListener(this);        findViewById(R.id.color_palette).setOnClickListener(this);        findViewById(R.id.eraser).setOnClickListener(this);        findViewById(R.id.reset).setOnClickListener(this);        findViewById(R.id.author).setOnClickListener(this);        buttonsLayout = findViewById(R.id.buttons_layout);        drawButtonsLayout = findViewById(R.id.draw_buttons_layout);        textViewAuthor = (TextView) findViewById(R.id.author);        textViewAuthor.setText(getName());    }    private void replay(View v) {        if (!drawView.isReplaying()) {            drawButtonsLayout.setVisibility(View.GONE);            v.setBackgroundResource(R.drawable.pause);        } else {            v.setBackgroundResource(R.drawable.replay);        }        try {            drawView.replay();        } catch (InterruptedException e) {            e.printStackTrace();        }    }    private void reset(View view) {        drawView.reset();    }    private void setEraser(View view) {        if (isEraserActive) {            drawView.setPen();            view.setBackgroundResource(R.drawable.clean);        } else {            drawView.setEraser();            view.setBackgroundResource(R.drawable.pencil);        }        isEraserActive = !isEraserActive;    }    private void selectPenColor(View view) {        ColorPaletteFragment.newInstance().show(getSupportFragmentManager(), ColorPaletteFragment.TAG);    }    @Override    protected void onPause() {        super.onPause();        drawView.onPause();    }    @Override    protected void onResume() {        super.onResume();        drawView.onResume();    }    @Override    public void onColorSelected(int color) {        drawView.setPaintColor(color);    }    @Override    public void onClick(View v) {        v.startAnimation(animClick);        switch (v.getId()) {            case R.id.screenshot:                handleScreenshot();                break;            case R.id.reset:                reset(v);                break;            case R.id.replay:                replay(v);                break;            case R.id.eraser:                setEraser(v);                break;            case R.id.color_palette:                selectPenColor(v);                break;            case R.id.author:                changeName(v);                break;        }    }    private void changeName(View v) {        final AlertDialog.Builder alert = new AlertDialog.Builder(this);        alert.setMessage(getString(R.string.enter_your_name));        final EditText input = new EditText(this);        alert.setView(input);        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {            public void onClick(DialogInterface dialog, int whichButton) {                String text = input.getText().toString();                textViewAuthor.setText(text);                saveName(text);            }        });        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {            public void onClick(DialogInterface dialog, int whichButton) {                dialog.dismiss();            }        });        alert.show();    }    private void saveName(String value) {        final SharedPreferences pref = getSharedPreferences(getPackageName(), MODE_PRIVATE);        pref.edit().putString(KEY_NAME, value).commit();    }    private String getName() {        final SharedPreferences pref = getSharedPreferences(getPackageName(), MODE_PRIVATE);        final String value = pref.getString(KEY_NAME, getString(R.string.click_change_name));        return (value.isEmpty() ? getString(R.string.click_change_name) : value);    }    private void handleScreenshot() {        Uri uri = takeScreenShot();        Intent share = new Intent(Intent.ACTION_SEND);        share.setType("image/jpeg");        share.putExtra(Intent.EXTRA_STREAM, uri);        startActivityForResult(Intent.createChooser(share, getString(R.string.share)), SHARE_REQUEST_CODE);    }    @Override    protected void onActivityResult(int requestCode, int resultCode, Intent data) {        super.onActivityResult(requestCode, resultCode, data);        if (requestCode == SHARE_REQUEST_CODE) {            deleteAllFiles(IMAGE_TEMP_PATH);        }    }    private void deleteAllFiles(String path) {        final File dir = new File(path);        if (dir.isDirectory()) {            String[] children = dir.list();            for (int i = 0; i < children.length; i++) {                new File(dir, children[i]).delete();            }        }    }    public Uri takeScreenShot() {        buttonsLayout.setVisibility(View.GONE);        final Time t = new Time();        t.setToNow();        rootView.setDrawingCacheEnabled(true);        final File folder = new File(IMAGE_TEMP_PATH);        if (!folder.exists()) {            folder.mkdir();        }        final String path = folder + "/" + t.format("%Y%M%d%k%M%S") + ".jpeg";        final Bitmap result = overlay(rootView.getDrawingCache(), drawView.getBitmap());        rootView.setDrawingCacheEnabled(false);        OutputStream fout;        final File imageFile = new File(path);        try {            fout = new FileOutputStream(imageFile);            result.compress(Bitmap.CompressFormat.JPEG, 90, fout);            fout.flush();            fout.close();        } catch (FileNotFoundException e) {            e.printStackTrace();        } catch (IOException e) {            e.printStackTrace();        } finally {            result.recycle();        }        buttonsLayout.setVisibility(View.VISIBLE);        return Uri.fromFile(new File(path));    }    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {        final Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());        final Canvas canvas = new Canvas(bmOverlay);        canvas.drawBitmap(bmp1, new Matrix(), null);        final int paddingLeft = (int) getResources().getDimension(R.dimen.draw_view_padding_left);        final int paddingTop = (int) getResources().getDimension(R.dimen.draw_view_padding_top);        canvas.drawBitmap(bmp2, paddingLeft, paddingTop, null);        return bmOverlay;    }    @Override    public void onReplayCompleted() {        drawButtonsLayout.setVisibility(View.VISIBLE);        buttonPlay.setBackgroundResource(R.drawable.replay);    }    @Override    public void onPaused() {        buttonPlay.setBackgroundResource(R.drawable.replay);    }    @Override    protected void onDestroy() {        super.onDestroy();        drawView.onDestroy();    }    @Override    public void onPlaying() {        buttonPlay.setBackgroundResource(R.drawable.pause);    }}