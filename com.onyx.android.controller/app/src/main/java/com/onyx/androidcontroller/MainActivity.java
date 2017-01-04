package com.onyx.androidcontroller;

import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.niqdev.mjpeg.DisplayMode;
import com.github.niqdev.mjpeg.Mjpeg;
import com.github.niqdev.mjpeg.MjpegInputStream;
import com.github.niqdev.mjpeg.MjpegView;

import java.io.IOException;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    private MjpegView mjpegView;
    private ToggleButton connectToggle;
    private boolean connected;
    private String ip = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupOnEditTextEvent();

        mjpegView = (MjpegView) findViewById(R.id.mjpeg_view);
        connectToggle = (ToggleButton) findViewById(R.id.toggleButton);
    }

    @SuppressWarnings("WeakerAccess")
    public void connect(View v) {
        if (!connected) {
            Log.w(getString(R.string.app_name), "Attempting to connect to Quadcopter. ");
            int TIMEOUT = 15; //seconds
            if (ip == null || !ip.matches(Constants.IPADDRESS_PATTERN)) {
                Log.w(getString(R.string.app_name), "Failed attempting to connect to Quadcopter. Invalid IP address.");
                connectToggle.toggle();
            } else {
                Mjpeg.newInstance()
                        .open("http://" + ip + ":8080/stream/video.mjpeg", TIMEOUT)
                        .subscribe(new Action1<MjpegInputStream>() {
                            @Override
                            public void call(MjpegInputStream inputStream) {
                                mjpegView.setSource(inputStream);
                                mjpegView.setDisplayMode(DisplayMode.BEST_FIT);
                                mjpegView.showFps(true);
                            }
                        });
                connected = true;
            }
        } else {
            disconnect();
        }

    }

    public void disconnect() {
        if (connected && mjpegView.isStreaming()) {
            mjpegView.stopPlayback();
            connected = false;
        }
    }

    /**
     * Setup the OnEditText Event.
     */
    private void setupOnEditTextEvent() {
        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ip = v.getText().toString();
                }
                return false;
            }
        });
    }
}
