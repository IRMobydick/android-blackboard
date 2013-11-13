package com.orhanobut.android.blackboard.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.FrameLayout;

import com.orhanobut.android.blackboard.DrawView;
import com.orhanobut.android.blackboard.MainActivity;
import com.orhanobut.android.blackboard.R;


/**
 * Created by nr on 10.11.2013.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mMainActivity;
    private DrawView mDrawView;

    public MainActivityTest() {
        super(MainActivity.class);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMainActivity = getActivity();

    }


    public void testPreconditions() {
        assertNotNull(mMainActivity);
    }


    public void testLayout() {
        FrameLayout layout = (FrameLayout) mMainActivity.findViewById(R.id.frameLayoutDraw);
        assertNotNull(layout);
    }




}

