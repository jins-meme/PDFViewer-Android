package com.jins_meme.pairing;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.jins_jp.meme.MemeConnectListener;
import com.jins_jp.meme.MemeLib;
import com.jins_jp.meme.MemeScanListener;

import java.util.ArrayList;
import java.util.List;

/**
 * ImageFragment.
 *
 * The MIT License
 * Copyright 2017 JINS Corp.
 */
public class MEMESettingsActivity extends AppCompatActivity implements MemeConnectListener {

    public static final String BUNDLE_PARAM_APP_ID = "app_id";
    public static final String BUNDLE_PARAM_SECRET = "app_secret";
    public static final String BUNDLE_RESULT_PARAM_MAC_ADDRESS = "mac_address";

    private static final int REQUEST_CODE_PLEASE_GRANT_PERMISSION = 10101;

    private MemeLib mMemeLib;
    private Handler mHandler = new Handler();

    private Button mBtnScan;
    private ListView mListView;
    private ArrayAdapter<String> mArrayAdapter;
    private List<String> mAddressList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_me_settings);


        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                stopScan();

                ArrayAdapter<String> adapter = (ArrayAdapter<String>) adapterView.getAdapter();
                String address = adapter.getItem(i);

                Intent intent = new Intent();
                intent.putExtra(BUNDLE_RESULT_PARAM_MAC_ADDRESS, address);
                setResult(RESULT_OK, intent);

                finish();
            }
        });

        mBtnScan = (Button) findViewById(R.id.btnScan);
        mBtnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScanAndStop();
            }
        });

        String appId = getIntent().getStringExtra(BUNDLE_PARAM_APP_ID);
        String appSecret = getIntent().getStringExtra(BUNDLE_PARAM_SECRET);


        MemeLib.setAppClientID(this, appId, appSecret);
        mMemeLib = MemeLib.getInstance();
        mMemeLib.setAutoConnect(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Bluetooth ON
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (!adapter.isEnabled()) {
                adapter.enable();
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_PLEASE_GRANT_PERMISSION);
        } else {
            startScanAndStop();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PLEASE_GRANT_PERMISSION) {
            // 許可されたかチェック
            for (int i = 0; i < permissions.length; i++) {
                final String permission = permissions[i];
                final int grantResult = grantResults[i];

                switch (permission) {
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            startScanAndStop();
                        } else {
                            Toast.makeText(this, "権限が許可されませんでした。", Toast.LENGTH_LONG).show();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void startScanAndStop() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBtnScan.setEnabled(false);
                startScan();
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                stopScan();
                mBtnScan.setEnabled(true);
            }
        }, 10 * 1000);

    }

    public void startScan() {

        mAddressList.clear();
        mMemeLib.setMemeConnectListener(this);

        mMemeLib.startScan(new MemeScanListener() {
            @Override
            public void memeFoundCallback(String s) {
                mAddressList.add(s);

                mArrayAdapter = new ArrayAdapter<String>(MEMESettingsActivity.this, R.layout.rowdata, mAddressList);
                mArrayAdapter.notifyDataSetChanged();
                mListView.setAdapter(mArrayAdapter);
            }
        });
    }

    public void stopScan() {
        if (mMemeLib != null && mMemeLib.isScanning()) {
            mMemeLib.stopScan();
        }
    }

    @Override
    public void memeConnectCallback(boolean b) {

    }

    @Override
    public void memeDisconnectCallback() {
    }
}