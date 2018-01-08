package com.jins_meme.pdfviewer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.jins_jp.meme.MemeConnectListener;
import com.jins_jp.meme.MemeLib;
import com.jins_jp.meme.MemeRealtimeData;
import com.jins_jp.meme.MemeRealtimeListener;
import com.jins_meme.pairing.MEMESettingsActivity;
import com.jins_meme.swing.NeckOperation;
import com.jins_meme.swing.YawPeak;

import java.util.ArrayList;


/**
 * FullscreenActivity.
 * <p>
 * The MIT License
 * Copyright 2017 JINS Corp.
 */
public class FullscreenActivity extends AppCompatActivity implements MemeRealtimeListener, MemeConnectListener {

    /**
     * 取得したAPP_IDを指定してください。
     */
    private static final String APP_ID = "";
    /**
     * 取得したAPP_SECRETを指定してください。
     */
    private static final String APP_SECRET = "";

    private static final String PDF_FILE_NAME = "01bj.pdf";

    private static final int ACTIVITY_REQUEST_CODE_MEME_SETTINGS = 1200;


    private MemeLib mMemeLib;
    private String mMemeAddress;
    private NeckOperation mNeckOperation;
    private Handler mHandler = new Handler();

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private ImageViewPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
//        mControlsView = findViewById(R.id.fullscreen_content_controls);
//        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
//        mContentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        FragmentManager manager = getSupportFragmentManager();
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        mAdapter = new ImageViewPagerAdapter(manager, this);
        viewPager.setAdapter(mAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        mNeckOperation = new NeckOperation();
        mNeckOperation.setListener(new NeckOperation.NeckOperationListener() {

            @Override
            public void didPeakNeck(NeckOperation.NeckOperationDirection direction, YawPeak last, ArrayList<YawPeak> yawPeakSummary) {
                if (direction == NeckOperation.NeckOperationDirection.NeckOperationDirectionRight) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                        }
                    });


                } else if (direction == NeckOperation.NeckOperationDirection.NeckOperationDirectionLeft) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAdapter.open(PDF_FILE_NAME);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mAdapter.close();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
//        // Hide UI first
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.hide();
//        }
//        mControlsView.setVisibility(View.GONE);
//        mVisible = false;
//
//        // Schedule a runnable to remove the status and navigation bar after a delay
//        mHideHandler.removeCallbacks(mShowPart2Runnable);
//        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
//        // Show the system bar
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//        mVisible = true;
//
//        // Schedule a runnable to display UI elements after a delay
//        mHideHandler.removeCallbacks(mHidePart2Runnable);
//        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    protected PdfRenderer.Page getPage(int p) {
        return mAdapter.getPage(p);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings_meme:
                Intent intent = new Intent(this, MEMESettingsActivity.class);

                intent.putExtra(MEMESettingsActivity.BUNDLE_PARAM_APP_ID, APP_ID);
                intent.putExtra(MEMESettingsActivity.BUNDLE_PARAM_SECRET, APP_SECRET);

                startActivityForResult(intent, ACTIVITY_REQUEST_CODE_MEME_SETTINGS);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_REQUEST_CODE_MEME_SETTINGS) {

            String address = data.getStringExtra(MEMESettingsActivity.BUNDLE_RESULT_PARAM_MAC_ADDRESS);
            mMemeAddress = address;

            // Memeの設定と初期化
            if (mMemeLib != null) {
                mMemeLib.disconnect();
            }

            MemeLib.setAppClientID(this, APP_ID, APP_SECRET);
            mMemeLib = MemeLib.getInstance();
            mMemeLib.setAutoConnect(false);
            mMemeLib.connect(address);
            mMemeLib.setMemeConnectListener(this);

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mMemeLib != null && mMemeLib.isConnected()) {
            mMemeLib.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMemeAddress != null && !mMemeLib.isConnected()) {
            mMemeLib.connect(mMemeAddress);
        }
    }

    @Override
    public void memeConnectCallback(boolean b) {
        mMemeLib.startDataReport(this);
    }

    @Override
    public void memeDisconnectCallback() {
    }

    @Override
    public void memeRealtimeCallback(MemeRealtimeData memeRealtimeData) {
        mNeckOperation.addMemeRealtimeData(memeRealtimeData);
    }
}
