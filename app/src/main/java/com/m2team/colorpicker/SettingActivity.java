package com.m2team.colorpicker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;
import com.rey.material.widget.Button;
import com.rey.material.widget.Spinner;

import java.util.Set;

public class SettingActivity extends AppCompatActivity implements Spinner.OnItemClickListener {
    Spinner spn_1st_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        spn_1st_mode = (Spinner) findViewById(R.id.spinner_1st_color_type);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_spn, getResources().getStringArray(R.array.color_mode));
        adapter.setDropDownViewResource(R.layout.row_spn_dropdown);
        int most = Utils.getPrefInt(this, Constant.MOST_COLOR_MODE);

        spn_1st_mode.setAdapter(adapter);
        spn_1st_mode.setOnItemClickListener(this);
        spn_1st_mode.setSelection(most);
        createActionbar();
    }

    @Override
    public boolean onItemClick(Spinner parent, View view, int position, long id) {
        parent.setSelection(position);
        return false;
    }

    private void createActionbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setTitle("Setting");
            View view = LayoutInflater.from(this).inflate(R.layout.layout_action_bar_setting, null);
            actionBar.setCustomView(view);
        }
    }

    @Override
    public void onBackPressed() {
        Utils.putPrefValue(SettingActivity.this, Constant.MOST_COLOR_MODE, spn_1st_mode.getSelectedItemPosition());
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Utils.putPrefValue(SettingActivity.this, Constant.MOST_COLOR_MODE, spn_1st_mode.getSelectedItemPosition());
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
