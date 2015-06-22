package com.m2team.colorpicker;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.m2team.colorpicker.utils.Applog;
import com.m2team.colorpicker.utils.Utils;
import com.rey.material.widget.Button;
import com.rey.material.widget.SnackBar;

import java.io.File;
import java.io.IOException;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_PICKUP = 2;
    Button btn_take_photo, btn_load_photo, btn_live_picker, btn_create_palette;
    SnackBar mSnackBar;
    Context mContext;
    String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        btn_take_photo = (Button) findViewById(R.id.btn_take_photo);
        btn_take_photo.setOnClickListener(this);
        btn_load_photo = (Button) findViewById(R.id.btn_load_photo);
        btn_load_photo.setOnClickListener(this);
        btn_live_picker = (Button) findViewById(R.id.btn_live_picker);
        btn_live_picker.setOnClickListener(this);
        btn_create_palette = (Button) findViewById(R.id.btn_create_palette);
        btn_create_palette.setOnClickListener(this);
        mSnackBar = (SnackBar) findViewById(R.id.snackbar);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        Intent mShareIntent = new Intent();
        mShareIntent.setAction(Intent.ACTION_SEND);
        mShareIntent.setType("text/plain");
        mShareIntent.putExtra(Intent.EXTRA_TEXT, "Download " + getString(R.string.app_name) + " from Google Play Store");
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(mShareIntent);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_take_photo:
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    dispatchTakePictureIntent();
                } else {
                    mSnackBar.applyStyle(R.style.SnackBarSingleLine)
                            .text("Your device does not support camera")
                            .duration(2000)
                            .show();
                }
                break;

            case R.id.btn_load_photo:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE_PICKUP);
                break;

            case R.id.btn_live_picker:
                Intent intentColorPickerActivity = new Intent(this, LiveColorPickerActivity.class);
                startActivity(intentColorPickerActivity);
                break;
            case R.id.btn_create_palette:
                intent = new Intent(this, CreatePaletteActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = Utils.createImageFile(mSnackBar, "Camera_" + System.currentTimeMillis());
            } catch (IOException ex) {
                Applog.e(ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                uri = photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                mSnackBar.applyStyle(R.style.SnackBarSingleLine)
                        .text("Have error when processing photo. Please try to take photo again")
                        .duration(2000)
                        .show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Intent intent = new Intent(mContext, ImageColorPickerActivity.class);
            intent.putExtra("uri", uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (requestCode == REQUEST_IMAGE_PICKUP && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String path = Utils.getPath(mContext, uri);
            Intent intent = new Intent(mContext, ImageColorPickerActivity.class);
            intent.putExtra("uri", path);
            intent.putExtra("uri_raw", uri);
            startActivity(intent);
        }
    }


}
