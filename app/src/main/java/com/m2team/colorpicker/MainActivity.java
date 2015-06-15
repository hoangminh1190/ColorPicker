package com.m2team.colorpicker;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rey.material.widget.Button;
import com.rey.material.widget.SnackBar;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_PICKUP = 2;
    Button btn_take_photo, btn_load_photo;
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
        mSnackBar = (SnackBar) findViewById(R.id.snackbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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

        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile2();
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
            Intent intent = new Intent(mContext, ColorPickerActivity.class);
            intent.putExtra("uri", uri);
            startActivity(intent);
        } else if (requestCode == REQUEST_IMAGE_PICKUP && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String path = Utils.getPath(mContext, uri);
            Applog.d("PAAAAAAA: " + path);
            Intent intent = new Intent(mContext, ColorPickerActivity.class);
            intent.putExtra("uri", path);
            intent.putExtra("uri_raw", uri);
            startActivity(intent);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "JPEG_" + System.currentTimeMillis();
        File dir = getExternalCacheDir();
        if (dir.exists()) {

        } else {
            dir.mkdir();
        }
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (dir.canRead() && dir.canWrite()) {
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    dir      /* directory */
            );
            return image;
        }
        return null;
    }

    private File createImageFile2() throws IOException {
        // Create an image file name
        String imageFileName = "JPEG_" + System.currentTimeMillis();
        File dir = new File(Environment.getExternalStorageDirectory() + "/CP/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    dir      /* directory */
            );
            return image;
        } else {
            mSnackBar.applyStyle(R.style.SnackBarSingleLine)
                    .text("Cannot create taken photo on your device storage")
                    .duration(2000)
                    .show();
        }
        return null;
    }
}
