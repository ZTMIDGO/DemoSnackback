package com.litesnap.open.snack;

import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SnackBackActivity extends AppCompatActivity {

    @Override
    public void setContentView(int layoutResID) {
        LayoutInflater inflater = LayoutInflater.from(SnackBackActivity.this);
        ViewGroup group = (ViewGroup) inflater.inflate(R.layout.snack_layout, null);
        group.addView(inflater.inflate(layoutResID, null));
        setContentView(group);
    }
}
