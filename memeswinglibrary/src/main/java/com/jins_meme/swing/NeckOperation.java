package com.jins_meme.swing;

import android.util.Log;

import com.jins_jp.meme.MemeRealtimeData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * NeckOperation.
 * <p>
 * The MIT License
 * Copyright 2017 JINS Corp.
 */
public class NeckOperation {
    public static String TAG = NeckOperation.class.getSimpleName();

    //操作イベントを通知するリスナー
    private NeckOperationListener listener;

    private ArrayList<MemeRealtimeData> memeRealtimeDataList;

    private ArrayList<YawPeak> yawPeak;
    private ArrayList<YawPeak> yawPeakSummary;

    //データチェックするカウント
    private static final int DATA_CHECK_COUNT = 4;

    private final float _p_yawd1_th = 2.6f;

    //ios は秒で処理していたが、Androidは ミリ秒としたので _yawPeackTimeDif * 1000 してミリ秒に変更
    private final float _yawPeackTimeDif = 0.3f * 1000;

    private long _yaw_ep;

    private ArrayList<Float> _yawd1ma3Peak;


    public enum NeckOperationDirection {
        NeckOperationDirectionUp,
        NeckOperationDirectionRight,
        NeckOperationDirectionDown,
        NeckOperationDirectionLeft,
    }

    public NeckOperation() {
        this.memeRealtimeDataList = new ArrayList<MemeRealtimeData>();

        this.yawPeakSummary = new ArrayList<YawPeak>();
        this.yawPeak = new ArrayList<YawPeak>();
        YawPeak y = new YawPeak();
        y.setDate(careteDummyDate());
        this.yawPeak.add(y);

        this._yawd1ma3Peak = new ArrayList<Float>();
        this._yawd1ma3Peak.add(0.0f);
    }

    //リスナーを設定する
    public void setListener(NeckOperationListener listener) {
        this.listener = listener;
    }

    public ArrayList<YawPeak> getYawPeakSummary() {
        return this.yawPeakSummary;
    }

    private Date careteDummyDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, -1);
        return cal.getTime();
    }

    //リストへ追加
    public void addMemeRealtimeData(MemeRealtimeData memeRealtimeData) {
        this.memeRealtimeDataList.add(memeRealtimeData);

        if (this.memeRealtimeDataList.size() > DATA_CHECK_COUNT) {
            //ここで yaw の値の確認
            yawCheck();
            this.memeRealtimeDataList.remove(0);
        }
    }

    //リセット
    public void reset() {
        this._yaw_ep = 0;
        this.memeRealtimeDataList.clear();

        this.yawPeak.clear();
        YawPeak y = new YawPeak();
        y.setDate(careteDummyDate());
        this.yawPeak.add(y);
        this.yawPeakSummary.clear();

    }

    //ここで yaw の値の確認 首振り判定
    private void yawCheck() {
        Log.d(TAG, "yawCheck");

        // 左右
        ArrayList<Float> yaw = new ArrayList<Float>();

        // 0 リストに積んだ MemeRealtimeData から yaw の値だけ取り出す?
        for (MemeRealtimeData d : memeRealtimeDataList) {
            yaw.add(d.getYaw());
        }
        Log.d(TAG, "yaw " + yaw);

        // 2 判定開始時の値を保存
        ArrayList<Float> yawm1 = new ArrayList<Float>();
        yawm1.add(yaw.get(0));

        // 1
        for (int i = 1; i < yaw.size(); i++) {
            yawm1.add(yaw.get(i - 1));
        }
        Log.d(TAG, "yawm1 " + yawm1);

        // 3 yaw2
        ArrayList<Float> yaw2 = new ArrayList<Float>();

        int rotetions = 0;
        for (int i = 0; i < yaw.size(); i++) {
            if (Math.abs(yaw.get(i) - yawm1.get(i)) > 300) {
                if (yaw.get(i) - yawm1.get(i) > 0) {
                    rotetions = rotetions - 1;
                } else {
                    rotetions = rotetions + 1;
                }
            }

            yaw2.add(rotetions * 360 + yaw.get(i));
        }
        Log.d(TAG, "yaw2 " + yaw2);

        // 5 yawd1ma3
        ArrayList<Float> yawd1ma3 = new ArrayList<Float>();
        for (int i = 0; i < 2; i++) {
            float v = (float) (((yaw2.get(i) - yaw2.get(i + 1)) + (yaw2.get(i + 1) - yaw2.get(i + 2)) + (yaw2.get(i + 2) - yaw2.get(i + 3))) / 3.0);
            yawd1ma3.add(v);
        }
        //Log.d(TAG,"yawd1ma3 "+yawd1ma3);

        // 6 yaw_io,sign,sign_
        ArrayList<Integer> yaw_io = new ArrayList<Integer>();
        int count = 0;
        for (Float n : yawd1ma3) {
            //Log.d(TAG,"Math.abs(n) "+Math.abs(n));
            if (Math.abs(n) > _p_yawd1_th) {
                yaw_io.add(count, 1);
            } else {
                yaw_io.add(count, 0);
            }
            count++;
        }
        //Log.d(TAG,"yaw_io "+yaw_io);

        int sign = 0;
        if (yawd1ma3.get(0) >= 0.0) {
            sign = 1;
        } else if (yawd1ma3.get(0) < 0.0) {
            sign = -1;
        }

        int sign_ = 0;
        if (yawd1ma3.get(1) >= 0.0) {
            sign_ = 1;
        } else if (yawd1ma3.get(1) < 0.0) {
            sign_ = -1;
        }
        Log.d(TAG, "sign " + sign + " sign_ " + sign_);

        //7.カウンターyaw_epを作る
        if ((yaw_io.get(0) == 1 && yaw_io.get(1) == 0)
                || (yaw_io.get(0) == 1 && yaw_io.get(1) == 1 && sign != sign_)) {
            Log.d(TAG, "カウンターyaw_epを作る1");

            _yaw_ep++;

            this._yawd1ma3Peak.add(0, yawd1ma3.get(0));
            if (_yawd1ma3Peak.size() > 2) {
                _yawd1ma3Peak.remove(_yawd1ma3Peak.size() - 1);
            }
            Log.d(TAG, "_yawd1ma3Peak " + _yawd1ma3Peak);

            YawPeak last = yawPeak.get(yawPeak.size() - 1);

            // ピークバッファ
            long unixtime = System.currentTimeMillis();
            long unixtime_ = last.getDate().getTime();

            //int sign = 0;
            if (_yawd1ma3Peak.get(0) >= 0.0) {
                sign = 1;
            } else if (_yawd1ma3Peak.get(0) < 0.0) {
                sign = -1;
            }

            //int sign_ = 0;
            if (_yawd1ma3Peak.get(1) >= 0.0) {
                sign_ = 1;
            } else if (_yawd1ma3Peak.get(1) < 0.0) {
                sign_ = -1;
            }
            Log.d(TAG, "_yawd1ma3Peak " + sign + " " + sign_);

            YawPeak yawPeakDic = new YawPeak();
            yawPeakDic.setDate(new Date(unixtime));

            long t = (unixtime - unixtime_);
            if (t <= _yawPeackTimeDif && sign != sign_) {
                int c = last.getCount() + 1;
                yawPeakDic.setStartFlag(0);
                yawPeakDic.setCount(c);
                yawPeakDic.setSign(sign);
                yawPeakDic.setId(_yaw_ep);
                yawPeakDic.setSummaryFlag(0);
            } else {
                yawPeakDic.setStartFlag(1);
                yawPeakDic.setCount(1);
                yawPeakDic.setSign(sign);
                yawPeakDic.setId(_yaw_ep);
                yawPeakDic.setSummaryFlag(0);
            }

            // ピークサマリ
            if (yawPeakDic.getStartFlag() == 1
                    && unixtime - unixtime_ <= _yawPeackTimeDif
                    && last.getSummaryFlag() == 0) {

                // ピークサマリーに追加
                yawPeakSummary.add(last);
                last.setSummaryFlag(1);

                // コントロール
                operationRL(last);

                // ピークサマリーにれたのでピークの余分な行を消す
                int s = yawPeak.size() - 1;
                for (int i = 0; i < s; i++) {
                    yawPeak.remove(0);
                }
            }
            yawPeak.add(yawPeakDic);
        } else {
            //Log.d(TAG,"カウンターyaw_epを作る2");

            YawPeak last = yawPeak.get(yawPeak.size() - 1);

            double unixtime = (new Date()).getTime();
            double unixtime_ = last.getDate().getTime();

            if (unixtime - unixtime_ > _yawPeackTimeDif
                    && last.getSummaryFlag() == 0) {
                // ピークサマリーに追加
                yawPeakSummary.add(last);
                last.setSummaryFlag(1);

                // コントロール
                operationRL(last);

                // ピークサマリーにれたのでピークの余分な行を消す
                int s = yawPeak.size() - 1;
                for (int i = 0; i < s; i++) {
                    yawPeak.remove(0);
                }
            }

        }
    }

    // コントロール
    private void operationRL(YawPeak last) {
        Log.d(TAG, "operationRL");

        int sing = last.getSign();
        int count = last.getCount();
        //Log.d(TAG," sing "+sing);
        //Log.d(TAG," count "+count);

        if (count % 2 == 0) {
            sing = sing * 1;
        } else if (count % 2 == 1) {
            sing = sing * -1;
        }

        NeckOperationDirection direction;
        if (sing > 0) {
            direction = NeckOperationDirection.NeckOperationDirectionRight;
        } else {
            direction = NeckOperationDirection.NeckOperationDirectionLeft;
        }

        //ここで画面を更新する
        if (this.listener != null) {
            this.listener.didPeakNeck(direction, last, getYawPeakSummary());
        }

        // 削除
        if (yawPeakSummary.size() > 10) {
            yawPeakSummary.remove(0);
        }
    }

    public interface NeckOperationListener {
        void didPeakNeck(final NeckOperationDirection direction, final YawPeak last, final ArrayList<YawPeak> yawPeakSummary);
    }
}
