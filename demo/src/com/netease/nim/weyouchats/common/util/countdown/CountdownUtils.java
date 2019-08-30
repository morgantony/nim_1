package com.netease.nim.weyouchats.common.util.countdown;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class CountdownUtils {
    private int time = 60;

    private Timer timer;
    private TextView tvSure;
    private String tvText;

    public CountdownUtils(TextView tvSure, String tvText, int time) {
        super();
        this.tvSure = tvSure;
        this.tvText = tvText;
        this.time=time;
    }

    public void init() {
        time = 60;
    }

    public void runTimer() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                time--;
                Message msg = handler.obtainMessage();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        };
        timer.schedule(task, 100, 1000);
    }


    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    if (time > 0) {
                        String strTime = time > 9 ? String.valueOf(time) : "0" + time;
                        tvSure.setEnabled(false);
                        tvSure.setText("00:" + strTime);
                    } else {
                        timer.cancel();
                        tvSure.setText(tvText);
                        tvSure.setEnabled(true);
                    }
                    break;
                default:
                    break;
            }
        }

        ;
    };


}
