package com.jwplayer.demo.listview;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private PlayerAdapter mPlayerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a reference to the CoordinatorLayout
        ListView listView = (ListView) findViewById(R.id.list_view);
        mPlayerAdapter = new PlayerAdapter(this, listView, Samples.PLAYLIST);
    }

    @Override
    protected void onResume() {
        // Let JW Player know that the app has returned from the background
        mPlayerAdapter.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Let JW Player know that the app is going to the background
        mPlayerAdapter.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Let JW Player know that the app is being destroyed
        mPlayerAdapter.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // If we are in fullscreen mode, exit fullscreen mode when the user uses the back button.
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mPlayerAdapter.onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
