package com.m2team.colorpicker;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.m2team.colorpicker.utils.Applog;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.Button;
import com.rey.material.widget.SnackBar;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICKUP = 2;
    private SnackBar mSnackBar;
    private Context mContext;
    private String path;
    private Uri uri;
    private InterstitialAd mInterstitialAd;
    private int action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.ires_ad_home_colors_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                openActivity();
            }

        });
        requestNewInterstitial();
    }

    private void openActivity() {
        Intent intent = null;
        if (action == 2) {
            intent = new Intent(this, LiveColorPickerActivity.class);
        } else if (action == 3) {
            intent = new Intent(this, CreatePaletteActivity.class);
        } else if (action == 4) {
            intent = new Intent(mContext, MainSettingActivity.class);
            intent.putExtra("index", MainSettingActivity.Tab.MATERIAL_COLOR.ordinal());
        } else if (action == 5) {
            intent = new Intent(this, MainSettingActivity.class);
            intent.putExtra("index", MainSettingActivity.Tab.CONVERT.ordinal());
        }
        if (intent != null)
            startActivity(intent);
    }


    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.test_device_id_n910f))
                .addTestDevice(getString(R.string.test_device_id_htc))
                .build();

        mInterstitialAd.loadAd(adRequest);
    }


    private void init() {
        mContext = this;
        Button btn_take_photo = (Button) findViewById(R.id.btn_take_photo);
        btn_take_photo.setOnClickListener(this);
        Button btn_load_photo = (Button) findViewById(R.id.btn_load_photo);
        btn_load_photo.setOnClickListener(this);
        Button btn_live_picker = (Button) findViewById(R.id.btn_live_picker);
        btn_live_picker.setOnClickListener(this);
        Button btn_create_palette = (Button) findViewById(R.id.btn_create_palette);
        btn_create_palette.setOnClickListener(this);
        Button btn_material_colors = (Button) findViewById(R.id.btn_material_color_list);
        btn_material_colors.setOnClickListener(this);
        Button btn_convert = (Button) findViewById(R.id.btn_convert);
        btn_convert.setOnClickListener(this);
        mSnackBar = (SnackBar) findViewById(R.id.snackbar);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            btn_take_photo.setVisibility(View.GONE);
            btn_live_picker.setVisibility(View.GONE);
        } else {
            btn_take_photo.setVisibility(View.VISIBLE);
            btn_live_picker.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_license:
                final Notices notices = new Notices();
                notices.addNotice(new Notice("MaterialLibrary", "", "Copyright 2015 Rey Pham.", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Subsampling Scale Image View", "", "Copyright 2015 David Morrissey", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Universal Image Loader", "https://github.com/nostra13/Android-Universal-Image-Loader/", "Copyright 2011-2015 Sergey Tarasevich", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Camera Color Picker", "http://tvbarthel.github.io/CameraColorPicker/", "Copyright (C) 2014 tvbarthel", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Android-Material-Design-Colors", "https://github.com/wada811/Android-Material-Design-Colors/", "Copyright 2014 wada811", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Material Color Picker", "", "Copyright (c) 2015 Aidan Grabe", new MITLicense()));

                new LicensesDialog.Builder(this).setNotices(notices).setIncludeOwnLicense(true).build().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_take_photo:
                action = 0;
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    dispatchTakePictureIntent();
                } else {
                    Utils.showMessage(mSnackBar, "Your device does not support camera");
                }
                break;

            case R.id.btn_load_photo:
                action = 1;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE_PICKUP);
                break;

            case R.id.btn_live_picker:
                action = 2;
                if (mInterstitialAd.isLoaded()) mInterstitialAd.show();
                else openActivity();
                break;

            case R.id.btn_create_palette:
                action = 3;
                openActivity();
                break;

            case R.id.btn_material_color_list:
                action = 4;
                if (mInterstitialAd.isLoaded()) mInterstitialAd.show();
                else openActivity();
                break;

            case R.id.btn_convert:
                action = 5;
                openActivity();
                break;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Camera_" + System.currentTimeMillis());
            uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            path = Utils.getPath(mContext, uri);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (!TextUtils.isEmpty(path)) outState.putString("path", path);
        if (uri != null) outState.putString("uri", uri.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        path = savedInstanceState.getString("path");
        String str = savedInstanceState.getString("uri");
        if (!TextUtils.isEmpty(str)) uri = Uri.parse(str);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Intent intent = new Intent(mContext, ImageColorPickerActivity.class);
            if (data != null) {
                Uri resultURI = data.getData();
                if (resultURI == null) {
                    try {
                        resultURI = (Uri) data.getExtras().get("data");
                        if (resultURI != null) uri = resultURI;
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                        Applog.e("ClassCastException onActivityResult");
                    }
                }
            }
            intent.putExtra("path", path);
            intent.putExtra("uri", uri != null ? uri.toString() : "");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (requestCode == REQUEST_IMAGE_PICKUP && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri == null) {
                    try {
                        uri = (Uri) data.getExtras().get("data");
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                        Applog.e("ClassCastException onActivityResult");
                    }
                }
                String path = Utils.getPath(mContext, uri);
                Intent intent = new Intent(mContext, ImageColorPickerActivity.class);
                intent.putExtra("path", path);
                intent.putExtra("uri", uri != null ? uri.toString() : "");
                startActivity(intent);
            } else {
                mSnackBar.applyStyle(R.style.SnackBarMultiLine);
                Utils.showMessage(mSnackBar, "Error when choose photo. Please try again");
            }
        }
    }

    @Override
    public void onBackPressed() {
        Dialog.Builder builder = new SimpleDialog.Builder(R.style.SimpleDialogLight) {
            @Override
            public void onPositiveActionClicked(DialogFragment fragment) {
                finish();
                Utils.clearStringSet(MainActivity.this, Constant.COLOR_RECENT_LIST);
            }

        };

        ((SimpleDialog.Builder) builder).message("DO YOU WANT TO EXIT?")
                .positiveAction("OK")
                .negativeAction("CANCEL");
        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(getSupportFragmentManager(), null);
    }
}
