package com.m2team.colorpicker;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.m2team.colorpicker.utils.Applog;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PaletteBackgroundActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout view;
    private Bitmap bitmap = null;
    private Uri uri = null;
    private String imgPath ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette_background);
        view = (LinearLayout) findViewById(R.id.ll1);
        final AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest.Builder builder = new AdRequest.Builder();
        builder.addTestDevice(getString(R.string.test_device_id_n910f));
        builder.addTestDevice(getString(R.string.test_device_id_htc));
        AdRequest adRequest = builder.build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                Applog.e("Fail to load banner_ad_bottom_palette_bg_unit_id ads: " + errorCode);
                adView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });
        createActionbar();
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String colorPercent;
                int percent, color, totalPercent;
                int size = bundle.getInt("numberOfColors");
                totalPercent = bundle.getInt("totalPercent");
                if (totalPercent == 0) totalPercent = 100;
                view.setWeightSum(1);
                boolean isHorizontal = bundle.getBoolean("isHorizontal");
                if (isHorizontal)
                    view.setOrientation(LinearLayout.HORIZONTAL);
                else
                    view.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams colorParams;
                for (int i = 0; i < size; i++) {
                    colorPercent = intent.getStringExtra("colorPercent" + i);
                    percent = Integer.parseInt(colorPercent.split(Constant.VAR_TOKEN)[0]);
                    color = Integer.parseInt(colorPercent.split(Constant.VAR_TOKEN)[1]);
                    if (!isHorizontal)
                        colorParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, (percent * 1f / totalPercent));
                    else
                        colorParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (percent * 1f / totalPercent));

                    colorParams.setMargins(0, 0, 0, 0);

                    View colorView = new View(this);
                    colorView.setBackgroundColor(color);
                    colorView.setLayoutParams(colorParams);
                    view.addView(colorView);
                }
            }
        }
    }

    private void createActionbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_action_bar_pallete_background, null);
            view.findViewById(R.id.btn_set_wallpaper).setOnClickListener(this);
            view.findViewById(R.id.btn_download).setOnClickListener(this);
            view.findViewById(R.id.btn_share).setOnClickListener(this);
            actionBar.setCustomView(view);
            Toolbar parent = (Toolbar) view.getParent();
            parent.setContentInsetsAbsolute(0, 0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_wallpaper:
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(PaletteBackgroundActivity.this);
                try {
                    if (bitmap == null)
                        bitmap = loadBitmapFromView(view);
                    if (bitmap != null) {
                        wallpaperManager.setBitmap(bitmap);
                        Toast.makeText(this, "Set wallpaper successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Applog.e("error load bitmap from view ");
                        Toast.makeText(this, "Error when setting wallpaper. Please try again", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Applog.e("error setting wallpaper: " + e.getMessage());
                    Toast.makeText(this, "Error when setting wallpaper. Please try again", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_download:
                boolean compress = download();
                if (compress) {
                    String msg = TextUtils.isEmpty(imgPath) ? "Saved successfully" : "Saved to " + imgPath;
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error when saving wallpaper. Please try again", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btn_share:
                if (uri == null) {
                    boolean success = download();
                    if (!success) {
                        Toast.makeText(PaletteBackgroundActivity.this, "Error when sharing palette", Toast.LENGTH_SHORT).show();
                        Applog.e("Error when download URI to share palette");
                    }
                }
                Intent mShareIntent = new Intent();
                mShareIntent.setAction(Intent.ACTION_SEND);
                mShareIntent.setType("image/jpeg");
                mShareIntent.putExtra(Intent.EXTRA_TEXT, "Share from " + getString(R.string.app_name));
                mShareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(mShareIntent, "Share to"));
                break;
        }
    }

    private boolean download() {
        File imageFile;
        try {
            if (uri == null) {
                if (bitmap == null) {
                    bitmap = loadBitmapFromView(view);
                }
                if (bitmap != null) {
                    int index = Utils.getPrefInt(this, Constant.FILE_INDEX);
                    index++;
                    Utils.putPrefValue(this, Constant.FILE_INDEX, index);
                    String ddMMyy =  new SimpleDateFormat("ddMMyy", Locale.US).format(Calendar.getInstance().getTime());
                    imageFile = Utils.createImageFile("Palette_" + ddMMyy + "_" + index);
                    if (imageFile != null) {
                        uri = Uri.fromFile(imageFile);
                        imgPath = imageFile.getPath();
                        FileOutputStream fos = new FileOutputStream(imageFile);
                        return bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    }
                }
            } else {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private Bitmap loadBitmapFromView(View v) {
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();
        return Bitmap.createScaledBitmap(v.getDrawingCache(), width, height, true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
