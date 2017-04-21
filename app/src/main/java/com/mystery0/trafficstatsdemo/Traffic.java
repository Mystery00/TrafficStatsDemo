package com.mystery0.trafficstatsdemo;

import java.text.DecimalFormat;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;

public class Traffic extends android.support.v7.widget.AppCompatTextView
{

    private boolean mAttached;
    Handler mHandler;
    private final BroadcastReceiver mIntentReceiver;
    Runnable mRunnable;
    protected int mTrafficColor;
    Handler mTrafficHandler;
    TrafficStats mTrafficStats;
    boolean showTraffic;
    float speed;
    float totalRxBytes;

    class SettingsObserver extends ContentObserver
    {
        SettingsObserver(Handler handler)
        {
            super(handler);
        }

        void observe()
        {
            ContentResolver resolver = getContext().getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor("status_bar_traffic"), false, this);

        }
        @Override
        public void onChange(boolean selfChange)
        {
            updateSettings();
        }
    }

    public Traffic(Context context)
    {
        this(context, null);
    }

    public Traffic(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public Traffic(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mIntentReceiver = new Receiver();
        mRunnable = new Task();    // Some crazy single-line gymnastics to compensate for the smali modifications, which probably also explains why both classes are nameless (they aren't originally instantiated here)
        mHandler = new Handler();
        SettingsObserver settingsObserver = new SettingsObserver(mHandler);
        mTrafficStats = new TrafficStats();
        settingsObserver.observe();
        updateSettings();
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        if (!mAttached)
        {
            mAttached = true;
            mTrafficColor = getTextColors().getDefaultColor();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
        }
        updateSettings();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        if (mAttached)
        {
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
    }

    class Receiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals("android.net.conn.CONNECTIVITY_CHANGE"))
            {
                updateSettings();
            }
        }
    }

    public void updateTraffic()
    {
        mTrafficHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                speed = ((float) (TrafficStats.getTotalRxBytes()) - totalRxBytes) / 1024.0f / 3.0f;    // Speed in KiB/s
                totalRxBytes = (float) (TrafficStats.getTotalRxBytes());    // The smali uses mTrafficStats to call the method, unnecessary since it's a static one
                DecimalFormat DecimalFormalism = new DecimalFormat("###0");
                if ((speed / 1024.0f) >= 1.0f)
                    setText(DecimalFormalism.format((double) (speed / 1024.0f)) + "MB/s");    // Hooooly crap...
                else if (speed <= 0.0099)
                    setText(DecimalFormalism.format((double) (speed * 1024.0f)) + "B/s");
                else
                    setText(DecimalFormalism.format((double) speed) + "KB/s");

                update();
                super.handleMessage(msg);
            }
        };
        totalRxBytes = (float) TrafficStats.getTotalRxBytes();    // Ergh, this smells like a race condition just waiting to happen... Actually, is this even necessary?
        mTrafficHandler.sendEmptyMessage(0);
    }

    private boolean getConnectAvailable()
    {
        try
        {
            ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService("connectivity");
            return connectivityManager.getActiveNetworkInfo().isConnected();
        } catch (Exception e)
        {
        }

        return false;
    }

    public void update()
    {
        mTrafficHandler.removeCallbacks(mRunnable);
        mTrafficHandler.postDelayed(mRunnable, 3000);    // Poll once every three seconds
    }

    class Task implements Runnable
    {
        @Override
        public void run()
        {
            mTrafficHandler.sendEmptyMessage(0);
        }
    }

    private void updateSettings()
    {
        ContentResolver resolver = getContext().getContentResolver();
        int newColor = 0;

        newColor = Settings.System.getInt(resolver, "status_bar_traffic_color", mTrafficColor);

        if (newColor < 0 && newColor != mTrafficColor)
        {
            mTrafficColor = newColor;
            setTextColor(mTrafficColor);
        }

        showTraffic = (Settings.System.getInt(resolver, "status_bar_traffic", 1) == 1);

        if (showTraffic && getConnectAvailable())
        {
            if (mAttached)
                updateTraffic();

            setVisibility(View.VISIBLE);
        } else
            setVisibility(View.GONE);
    }
}
