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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupOnEditTextEvent();

    }

    @SuppressWarnings("WeakerAccess")
    @UiThread
    public void connect(View v) {
        Log.w(getString(R.string.app_name), "Attempting to connect to Quadcopter. " + v.toString());
    }

    /**
     * Setup the OnEditText Event.
     */
    private void setupOnEditTextEvent() {
        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    handled = true;
                    connect(v);
                }
                return handled;
            }
        });
    }
}
