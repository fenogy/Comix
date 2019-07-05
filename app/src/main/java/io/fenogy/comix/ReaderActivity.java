package io.fenogy.comix;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;


import java.io.File;

import io.fenogy.comix.fragment.ReaderFragment;


public class ReaderActivity extends AppCompatActivity {

    public static boolean LANDSCAPE_MODE;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(LANDSCAPE_MODE)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         }
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_reader);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_reader);
        setSupportActionBar(toolbar);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (savedInstanceState == null) {
            if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                ReaderFragment fragment = ReaderFragment.create(new File(getIntent().getData().getPath()));
                setFragment(fragment);
            }
            else {
                Bundle extras = getIntent().getExtras();
                ReaderFragment fragment = null;
                ReaderFragment.Mode mode = (ReaderFragment.Mode) extras.getSerializable(ReaderFragment.PARAM_MODE);

                if (mode == ReaderFragment.Mode.MODE_LIBRARY) {
                    fragment = ReaderFragment.create(extras.getInt(ReaderFragment.PARAM_HANDLER));
                }
                else {
                    fragment = ReaderFragment.create((File) extras.getSerializable(ReaderFragment.PARAM_HANDLER));
                }
                setFragment(fragment);
            }

//            if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
//                ReaderFragment fragment = ReaderFragment.create(new File(getIntent().getData().getPath()));
//                setFragment(fragment);
//            }
//            else {
//                Bundle extras = getIntent().getExtras();
//                ReaderFragment fragment = null;
//                ReaderFragment.Mode mode = (ReaderFragment.Mode) extras.getSerializable(ReaderFragment.PARAM_MODE);
//
//                if (mode == ReaderFragment.Mode.MODE_LIBRARY) {
//                    fragment = ReaderFragment.create(extras.getInt(ReaderFragment.PARAM_HANDLER));
//                }
//                else {
//                    fragment = ReaderFragment.create((File) extras.getSerializable(ReaderFragment.PARAM_HANDLER));
//                }
//                setFragment(fragment);
//            }
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame_reader, fragment)
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
