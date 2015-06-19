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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.m2team.colorpicker.function.ColorDetectDialogFragment;
import com.m2team.colorpicker.utils.Applog;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;
import com.rey.material.widget.Button;
import com.rey.material.widget.SnackBar;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageColorPickerActivity extends AppCompatActivity implements View.OnClickListener {
    private final int TIME_TOAST = 1500;
    Button btn_detect_color;
    SubsamplingScaleImageView imageView;
    TextView txtTextView;
    View bg_color;
    SnackBar mSnackBar;
    Context mContext;
    Bitmap bitmap;
    Uri uri;
    String fullInfoColor, favoriteColor;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_color_picker);
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
            actionBar.setTitle("");
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_action_bar, null);
            view.findViewById(R.id.btn_copy).setOnClickListener(this);
            view.findViewById(R.id.btn_bookmark).setOnClickListener(this);
            view.findViewById(R.id.btn_detail).setOnClickListener(this);
            view.findViewById(R.id.btn_color_recent_list).setOnClickListener(this);
            btn_detect_color = (Button) view.findViewById(R.id.btn_detect_color);
            btn_detect_color.setOnClickListener(this);
            btn_detect_color.setEnabled(false);
            actionBar.setCustomView(view);
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


    private void getColorPixel(Bitmap bitmap, int x, int y) {
        int pixel = bitmap.getPixel(x, y);
        fullInfoColor = Utils.setFullInfoColor(pixel);
        favoriteColor = Utils.getOneColorMode(fullInfoColor, Utils.getPrefInt(mContext, Constant.MOST_COLOR_MODE));
        if (!TextUtils.isEmpty(favoriteColor)) {
            String hex = Utils.getOneColorMode(fullInfoColor, 0);
            String[] hsv = Utils.getOneColorMode(fullInfoColor, 2).split(Constant.DOLLAR_TOKEN);
            if (hsv.length == 3)
                txtTextView.setText("HEX: " + hex + "\n"
                                + "RGB: " + Color.red(pixel) + " " + Color.green(pixel) + " " + Color.blue(pixel) + "\n"
                                + "HSV: " + Utils.roundZero(hsv[0]) + Constant.degree + " " + Utils.roundZero(Float.parseFloat(hsv[1]) * 100) + "% " + Utils.roundZero(Float.parseFloat(hsv[2]) * 100) + "%"
                );
            txtTextView.setTextColor(Color.parseColor(hex));
            bg_color.setBackgroundColor(Color.parseColor(hex));
        }
    }

    private void onPixelChange(MotionEvent e) {
        if (imageView.isReady()) {
            PointF sCoord = imageView.viewToSourceCoord(e.getX(), e.getY());
            if (sCoord.y < 0 || sCoord.x < 0 || sCoord.x > bitmap.getWidth() || sCoord.y > bitmap.getHeight()) {
                Utils.showMessage(mSnackBar, "You must select pixel inside the photo");
                return;
            }
            getColorPixel(bitmap, (int) sCoord.x, (int) sCoord.y);
        } else {
            Applog.e("Error imageview is not ready");
            Utils.showMessage(mSnackBar, "Error when processing image");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_detect_color:
                if (bitmap != null) {
                    Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            List<Palette.Swatch> swatches = palette.getSwatches();
                            ArrayList<Integer> colorIds = new ArrayList<Integer>();
                            for (Palette.Swatch swatch : swatches) {
                                colorIds.add(swatch.getRgb());
                            }
                            ColorDetectDialogFragment dialog = new ColorDetectDialogFragment();
                            Bundle bundle = new Bundle();
                            bundle.putIntegerArrayList("detectColors", colorIds);
                            dialog.setArguments(bundle);
                            dialog.show(getSupportFragmentManager(), "detectDialog");
                        }
                    });
                }
                break;
            case R.id.btn_copy:
                Utils.copyToClipboard(mContext, favoriteColor);
                Utils.putSharedPrefStringSetValue(this, Constant.COLOR_RECENT_LIST, favoriteColor);
                Utils.showMessage(mSnackBar, "Copied " + favoriteColor + " to clipboard");
                break;
            case R.id.btn_bookmark:
                if (TextUtils.isEmpty(favoriteColor)) {
                    Utils.showMessage(mSnackBar, "You must select one color to bookmark");
                    return;
                }
                Utils.putSharedPrefStringSetValue(mContext, Constant.COLOR_BOOKMARK_LIST, favoriteColor);
                Utils.showMessage(mSnackBar, "Added " + favoriteColor + " to bookmark");
                break;
            case R.id.btn_color_recent_list:
                Intent intent = new Intent(this, MainSettingActivity.class);
                intent.putExtra("index", MainSettingActivity.Tab.RECENT_COLOR.ordinal());
                startActivity(intent);
                break;
            case R.id.btn_detail:
                if (TextUtils.isEmpty(favoriteColor)) {
                    Utils.showMessage(mSnackBar, "Select at least one color");
                } else {
                    intent = new Intent(this, MainSettingActivity.class);
                    intent.putExtra("index", MainSettingActivity.Tab.DETAIL.ordinal());
                    intent.putExtra("detailColor", fullInfoColor);
                    startActivity(intent);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_picker, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        if (TextUtils.isEmpty(favoriteColor)) {
            Utils.showMessage(mSnackBar, "You must select one color to share");
        } else {
            // Fetch and store ShareActionProvider
            Intent mShareIntent = new Intent();
            mShareIntent.setAction(Intent.ACTION_SEND);
            mShareIntent.setType("text/plain");
            mShareIntent.putExtra(Intent.EXTRA_TEXT, favoriteColor);
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(mShareIntent);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent = new Intent(mContext, MainSettingActivity.class);
        switch (id) {
            case R.id.action_settings:
                intent.putExtra("index", MainSettingActivity.Tab.DETAIL.ordinal());
                break;
            case R.id.action_bookmark_list:
                intent.putExtra("index", MainSettingActivity.Tab.BOOKMARK.ordinal());
                break;
            case R.id.action_convert:
                intent.putExtra("index", MainSettingActivity.Tab.CONVERT.ordinal());
                break;
            case R.id.action_popular_color:
                intent.putExtra("index", MainSettingActivity.Tab.MATERIAL_COLOR.ordinal());
                break;
            case R.id.action_compare:
                intent.putExtra("index", MainSettingActivity.Tab.COMPARE.ordinal());
                break;
            case R.id.action_dialog_color:
                intent.putExtra("index", MainSettingActivity.Tab.PALETTE_BACKGROUND.ordinal());
                break;
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
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
                btn_detect_color.setEnabled(true);
            } else {
                Utils.showMessage(mSnackBar, "Wrong type or error photo. Please select another photo");
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
