package com.m2team.colorpicker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.rey.material.widget.SnackBar;

import java.io.FileNotFoundException;
import java.io.InputStream;


public class ColorPickerActivity extends AppCompatActivity {
    private final int TIME_TOAST = 1500;
    SubsamplingScaleImageView imageView;
    TextView txtTextView;
    View bg_color;
    SnackBar mSnackBar;
    Context mContext;
    Bitmap bitmap;
    Uri uri;
    ColorSpaceConverter converter = new ColorSpaceConverter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        mContext = this;
        imageView = (SubsamplingScaleImageView) findViewById(R.id.img_view);
        txtTextView = (TextView) findViewById(R.id.txt_color);
        mSnackBar = (SnackBar) findViewById(R.id.snackbar);
        bg_color = findViewById(R.id.bg_color);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String path = bundle.getString("uri");
                uri = (Uri) bundle.get("uri_raw");
                Applog.d("uri " + path);
                Applog.d("uri raw " + uri);
                LoadBitmapAsyncTask loadBitmapAsyncTask = new LoadBitmapAsyncTask();
                loadBitmapAsyncTask.execute(path);
            }
        }
        initGesture();
        createActionbar();
    }

    private void createActionbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.layout_action_bar);
        }
    }

    private void initGesture() {
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                onPixelChange(e);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (imageView.isReady()) {
                    PointF sCoord = imageView.viewToSourceCoord(e.getX(), e.getY());
                    Toast.makeText(getApplicationContext(), "Long press: " + ((int) sCoord.x) + ", " + ((int) sCoord.y), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Long press: Image not ready", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return super.onDoubleTap(e);
                /*if (imageView.isReady()) {
                    PointF sCoord = imageView.viewToSourceCoord(e.getX(), e.getY());
                    Toast.makeText(getApplicationContext(), "Double tap: " + ((int)sCoord.x) + ", " + ((int)sCoord.y), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Double tap: Image not ready", Toast.LENGTH_SHORT).show();
                }*/
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                onPixelChange(e2);
                return false;

            }
        });
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
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

    private void getColorPixel(Bitmap bitmap, int x, int y) {
        int pixel = bitmap.getPixel(x, y);
        String hex = String.format("#%02x%02x%02x", Color.red(pixel), Color.green(pixel), Color.blue(pixel));
        bg_color.setBackgroundColor(Color.parseColor(hex));
        float[] hsv = new float[3];
        Color.colorToHSV(pixel, hsv);
        Applog.d(hex);
        Applog.d("rgb: " + Color.red(pixel) + " " + Color.green(pixel) + " " + Color.blue(pixel));
        Applog.d("H: " + hsv[0] + " S: " + hsv[1] + " V: " + hsv[2]);
        float[] cmyk = ColorSpaceConverter.rgbToCmyk(new float[]{Color.red(pixel), Color.green(pixel), Color.blue(pixel)});
        Applog.d(cmyk[0] + " " + cmyk[1] + " " + cmyk[2] + " " + cmyk[3]);
        float[] hsl = ColorSpaceConverter.rgbToHSL(new float[]{Color.red(pixel), Color.green(pixel), Color.blue(pixel)});
        double[] lab = converter.RGBtoLAB(new int[]{Color.red(pixel), Color.green(pixel), Color.blue(pixel)});
        double[] xyz = converter.RGBtoXYZ(new int[]{Color.red(pixel), Color.green(pixel), Color.blue(pixel)});
        txtTextView.setText("H: " + hsv[0] + " S: " + hsv[1] + " V: " + hsv[2] + "\n"
                + "CMYK: " + cmyk[0] + " " + cmyk[1] + " " + cmyk[2] + " " + cmyk[3] + "\n"
                + "HSL: " + hsl[0] + " " + hsl[1] + " " + hsl[2]);
        Applog.d("HSL: " + hsl[0] + " " + hsl[1] + " " + hsl[2]);
        txtTextView.setTextColor(Color.parseColor(hex));
    }

    private void onPixelChange(MotionEvent e) {
        if (imageView.isReady()) {
            PointF sCoord = imageView.viewToSourceCoord(e.getX(), e.getY());
            if (sCoord.y < 0 || sCoord.x < 0 || sCoord.x > bitmap.getWidth() || sCoord.y > bitmap.getHeight()) {
                showMessage("You must select pixel inside the photo");
                return;
            }
            getColorPixel(bitmap, (int) sCoord.x, (int) sCoord.y);
        } else {
            Applog.e("Error imageview is not ready");
            showMessage("Error when processing image");
        }
    }

    private void showMessage(String text) {
        mSnackBar.applyStyle(R.style.SnackBarSingleLine)
                .text(text)
                .duration(TIME_TOAST).actionText("")
                .show();
    }

    private class LoadBitmapAsyncTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = BitmapFactory.decodeFile(params[0]);
            if (bitmap == null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Applog.e("get stream error");
                }
            }
            return bitmap;
            //return scaleBitmap(params[0], 1920, 1080);
        }

        @Override
        protected void onPostExecute(Bitmap bmp) {
            if (bmp != null) {
                imageView.setImage(ImageSource.bitmap(bmp));
                bitmap = bmp;
            } else {
                showMessage("Wrong type or error photo. Please select another photo");
                Applog.e("bitmap null");
            }
        }

        private Bitmap scaleBitmap(String uri, int targetW, int targetH) {
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            return BitmapFactory.decodeFile(uri, bmOptions);
        }
    }
}
