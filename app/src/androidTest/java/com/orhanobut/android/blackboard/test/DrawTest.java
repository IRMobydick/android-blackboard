package com.orhanobut.android.blackboard.test;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;

import com.orhanobut.android.blackboard.DrawView;
import com.orhanobut.android.blackboard.MainActivity;
import com.orhanobut.android.blackboard.R;


/**
 * Created by nr on 10.11.2013.
 */
public class DrawTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final int COLOR_PICK = 0;
    private MainActivity mMainActivity;
    private DrawView mDrawView;
    private Button mButtonReplay;
    private Button mButtonClear;
    private Button mButtonReset;
    private Button mButtonColorPalette;
    private Instrumentation mInstrument;
    private View mRootView;
    private Animation mAnimClick;

    public DrawTest() {
        super(MainActivity.class);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMainActivity = getActivity();
        mButtonReplay = (Button) mMainActivity.findViewById(R.id.replay);
        mButtonClear = (Button) mMainActivity.findViewById(R.id.eraser);
        mButtonColorPalette = (Button) mMainActivity.findViewById(R.id.color_palette);
        mButtonReset = (Button) mMainActivity.findViewById(R.id.reset);

        mInstrument = getInstrumentation();

        mRootView = mMainActivity.getWindow().getDecorView().getRootView();
        mAnimClick = AnimationUtils.loadAnimation(mMainActivity, R.anim.anim_alpha);

    }


    public void testPreconditions() {
        assertNotNull(mMainActivity);
        assertNotNull(mButtonClear);
        assertNotNull(mButtonClear);
        assertNotNull(mButtonColorPalette);
        assertNotNull(mButtonReset);
        assertNotNull(mAnimClick);
        assertNotNull(mRootView);
    }


    @UiThreadTest
    public void testReplay() {
        assertTrue(mButtonReplay.performClick());
    }

    @UiThreadTest
    public void testReset() {
        assertTrue(mButtonReset.performClick());
    }

    @UiThreadTest
    public void testClear() {
        assertTrue(mButtonClear.performClick());
    }

    @UiThreadTest
    public void testOnCreate(){
        FrameLayout layout = (FrameLayout) mMainActivity.findViewById(R.id.draw_layout);
     //   mDrawView = new DrawView(mMainActivity);
      //  layout.addView(mDrawView);

    }

}

