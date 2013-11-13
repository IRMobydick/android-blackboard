package com.orhanobut.android.blackboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

    private static final int COLOR_PICK = 0 ;
    private DrawView mDrawView;
    private boolean mEraserActive;
    private View mRootView;
    private Animation mAnimClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout layout = (FrameLayout) findViewById(R.id.frameLayoutDraw);
        mDrawView = new DrawView(this);
        layout.addView(mDrawView);
        mRootView = getWindow().getDecorView().getRootView();

        mAnimClick = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);
    }

    public void replay(View view) {
        mRootView.setEnabled(false);
        mDrawView.replay();
        mRootView.setEnabled(true);
    }

    public void reset(View view){
        view.startAnimation(mAnimClick);
        mDrawView.reset();
    }

    public void setEraser(View view) {
        view.startAnimation(mAnimClick);
        if (mEraserActive) {
            mDrawView.setPen();
            view.setBackgroundResource(R.drawable.clean);
        } else {
            mDrawView.setEraser();
            view.setBackgroundResource(R.drawable.pencil);
        }
        mEraserActive = !mEraserActive;
    }

    public void selectPenColor(View view) {
        view.startAnimation(mAnimClick);
        Intent i = new Intent(this, ColorPaletteActivity.class);
        startActivityForResult(i,COLOR_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (requestCode == COLOR_PICK){
            if (resultCode == RESULT_OK){
                int colorId = (int) data.getLongExtra("COLOR_ID", 0);
                mDrawView.setPaintColor(colorId);
            }
       }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDrawView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDrawView.onResume();
    }
}