package com.mystery0.trafficstatsdemo;

import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private TextView send;
    private TextView received;
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 222://send
                    Log.i(TAG, "handleMessage: " + msg.obj);
                    send.setText((String) msg.obj);
                    break;
                case 333://receive
                    Log.i(TAG, "handleMessage: " + msg.obj);
                    received.setText((String) msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        send = (TextView) findViewById(R.id.send);
        received = (TextView) findViewById(R.id.received);
        Button button = (Button) findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

//                WindowManager windowManager= (WindowManager) getSystemService(WINDOW_SERVICE);
//                WindowManager.LayoutParams params=new WindowManager.LayoutParams();
//                params.type=WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//                // 设置flag
//
//                int flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
//                // | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//                // 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
//                params.flags = flags;
//                // 不设置这个弹出框的透明遮罩显示为黑色
//                params.format = PixelFormat.TRANSLUCENT;
//                // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
//                // 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
//                // 不设置这个flag的话，home页的划屏会有问题
//
//                params.width = WindowManager.LayoutParams.MATCH_PARENT;
//                params.height = WindowManager.LayoutParams.MATCH_PARENT;
//
//                params.gravity = Gravity.CENTER;
//                windowManager.addView();
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                            final long s = TrafficStats.getTotalRxBytes();
                            final long r = TrafficStats.getTotalTxBytes();
                            new Timer().schedule(new TimerTask()
                            {
                                @Override
                                public void run()
                                {
                                    Message message1 = new Message();
                                    message1.what = 222;
                                    Log.i(TAG, "run: " + String.valueOf(TrafficStats.getTotalRxBytes() - s));
                                    message1.obj = String.valueOf(TrafficStats.getTotalRxBytes() - s);
                                    handler.sendMessage(message1);
                                    Message message2 = new Message();
                                    message2.what = 333;
                                    Log.i(TAG, "run: " + String.valueOf(TrafficStats.getTotalTxBytes() - r));
                                    message2.obj = String.valueOf(TrafficStats.getTotalTxBytes() - r);
                                    handler.sendMessage(message2);
                                }
                            }, 1000);

                    }
                });
            }
        });
    }
}
