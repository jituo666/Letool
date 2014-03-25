package com.xjt.letool;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends LetoolActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
