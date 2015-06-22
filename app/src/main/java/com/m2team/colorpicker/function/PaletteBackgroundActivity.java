package com.m2team.colorpicker.function;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.m2team.colorpicker.R;
import com.m2team.colorpicker.utils.Applog;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PaletteBackgroundActivity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout view;
    Bitmap bitmap = null;
    Uri uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        view = (LinearLayout) inflater.inflate(R.layout.activity_palette_background, null, false);
        setContentView(view);
        createActionbar();
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String colorPercent;
                int percent, color;
                int size = bundle.getInt("numberOfColors");
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
                        colorParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, (float) (percent * 1.0 / 100));
                    else
                        colorParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (float) (percent * 1.0 / 100));

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
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setTitle("");
            View view = LayoutInflater.from(this).inflate(R.layout.layout_action_bar_pallete_background, null);
            view.findViewById(R.id.btn_set_wallpaper).setOnClickListener(this);
            view.findViewById(R.id.btn_download).setOnClickListener(this);
            actionBar.setCustomView(view);
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
                    wallpaperManager.setBitmap(bitmap);
                    Toast.makeText(this, "Set wallpaper success", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Applog.e("error setting wallpaper: " + e.getMessage());
                    Toast.makeText(this, "Error when setting wallpaper. Please try again", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_download:
                boolean compress = download();
                if (compress) {
                    Toast.makeText(this, "Download success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error when download wallpaper. Please try again", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private boolean download() {
        File imageFile;
        try {
            imageFile = Utils.createImageFile(null, "Palette_" + System.currentTimeMillis());
            if (bitmap == null) {
                bitmap = loadBitmapFromView(view);
            }
            uri = Uri.fromFile(imageFile);
            FileOutputStream fos = new FileOutputStream(imageFile);
            return bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_palette_background, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share_palette);
        if (uri == null) {
            boolean success = download();
            if (success) {
                // Fetch and store ShareActionProvider
                Intent mShareIntent = new Intent();
                mShareIntent.setAction(Intent.ACTION_SEND);
                mShareIntent.setType("image/*");
                mShareIntent.putExtra(Intent.EXTRA_TEXT, "Share from " + getString(R.string.app_name));
                mShareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(mShareIntent);
                }
            } else {
                Toast.makeText(this, "Error when share palette", Toast.LENGTH_SHORT).show();
                Applog.e("Error when download URI to share palette");
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
