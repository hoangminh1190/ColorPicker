package com.m2team.colorpicker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.m2team.colorpicker.function.ColorDetailDialogFragment;
import com.m2team.colorpicker.function.ColorDetectDialogFragment;
import com.m2team.colorpicker.function.LongPressDialogFragment;
import com.m2team.colorpicker.utils.Applog;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.ProgressView;
import com.rey.material.widget.SnackBar;
import com.rey.material.widget.Switch;
import java.util.ArrayList;
import java.util.List;

public class ImageColorPickerActivity extends AppCompatActivity implements View.OnClickListener {
    private SubsamplingScaleImageView imageView;
    private ProgressView progressView;
    private TextView txtTextView;
    private View bg_color;
    private SnackBar mSnackBar;
    private Switch switches_sw1;
    private Context mContext;
    private Bitmap bitmap;
    private String fullInfoColor;
    private String favoriteColor;
    private String sUri;
    private String path;
    private int index = 0;
    private PointF currentPointSelect;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;

    private final ImageLoadingListener listener = new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String s, View view) {
            Applog.d("start loading...");
            progressView.start();
        }

        @Override
        public void onLoadingFailed(String s, View view, FailReason failReason) {
            Applog.e("Failed");
            if (index == 0 && !TextUtils.isEmpty(path)) {
                imageLoader.loadImage(path, options, listener);
                index++;
                return;
            }
            progressView.stop();
            showErrorDialog();
        }

        @Override
        public void onLoadingComplete(String s, View view, Bitmap bmp) {
            Applog.d("onLoadingComplete");
            bitmap = bmp;
            imageView.setImage(ImageSource.bitmap(bmp));
            initGesture();
            progressView.stop();
        }

        @Override
        public void onLoadingCancelled(String s, View view) {
            Applog.d("onLoadingCancelled");
            showErrorDialog();
            progressView.stop();
        }
    };

    private void init() {
        mContext = this;
        progressView = (ProgressView) findViewById(R.id.progress);
        imageView = (SubsamplingScaleImageView) findViewById(R.id.img_view);
        txtTextView = (TextView) findViewById(R.id.txt_color);
        mSnackBar = (SnackBar) findViewById(R.id.snackbar);
        bg_color = findViewById(R.id.bg_color);
        TextView textView = (TextView) findViewById(R.id.txt_lock_zoom);
        switches_sw1 = (Switch) findViewById(R.id.switches_sw1);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switches_sw1.setChecked(!switches_sw1.isChecked());
            }
        });
        imageView.setPanEnabled(!switches_sw1.isChecked());
        switches_sw1.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                imageView.setPanEnabled(!checked);
                if (currentPointSelect != null)
                    imageView.setScaleAndCenter(imageView.getScale(), currentPointSelect);
            }
        });

        txtTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    if (!TextUtils.isEmpty(tv.getText())) {
                        Utils.copyToClipboard(mContext, tv.getText().toString());
                        Utils.putSharedPrefStringSetValue(mContext, Constant.COLOR_RECENT_LIST, favoriteColor);
                        Utils.showMessage(mSnackBar, "Copied to clipboard");
                    }

                }
            }
        });

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest.Builder builder = new AdRequest.Builder();
        builder.addTestDevice(getString(R.string.test_device_id_n910f));
        builder.addTestDevice(getString(R.string.test_device_id_htc));
        AdRequest adRequest = builder.build();
        adView.loadAd(adRequest);

        DisplayImageOptions.Builder displayBuilder = new DisplayImageOptions.Builder();
        displayBuilder.resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(300));
        options = displayBuilder.build();
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(mContext);
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(configuration);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_color_picker);
        init();

        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getStringExtra("path");
            sUri = intent.getStringExtra("uri");
            Applog.d("path:" + path);
            Applog.d("uri:" + sUri);
            processImagePath();
        } else {
            showErrorDialog();
        }
        createActionbar();
    }

    private void processImagePath() {
        if (!TextUtils.isEmpty(sUri)) {
            imageLoader.loadImage(sUri, options, listener);
        } else if (!TextUtils.isEmpty(path)) {
            imageLoader.loadImage(path, options, listener);
        }
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
            view.findViewById(R.id.btn_detect_color).setOnClickListener(this);
            actionBar.setCustomView(view);
        }
    }

    private void initGesture() {
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                onPixelChange(e);
                currentPointSelect = imageView.viewToSourceCoord(e.getX(), e.getY());
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                onPixelChange(e);
                currentPointSelect = imageView.viewToSourceCoord(e.getX(), e.getY());
                LongPressDialogFragment fragment = LongPressDialogFragment.newInstance(mContext);
                Bundle bundle = new Bundle();
                bundle.putString("value", favoriteColor);
                bundle.putString("fragmentId", "main");
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), "longPressDialog");
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                currentPointSelect = imageView.viewToSourceCoord(e.getX(), e.getY());
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                onPixelChange(e2);
                currentPointSelect = imageView.viewToSourceCoord(e2.getX(), e2.getY());
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
                                + "RGB: " + Utils.setStyleRGB(new int[]{Color.red(pixel), Color.green(pixel), Color.blue(pixel)}) + "\n"
                                + "HSV: " + Utils.setStyleHSV_HSL(hsv)
                );
            bg_color.setBackgroundColor(Color.parseColor(hex));
        }
    }

    private void onPixelChange(MotionEvent e) {
        if (imageView.isReady() && bitmap != null) {
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
                            ArrayList<Integer> colorIds = new ArrayList<>();
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
                if (TextUtils.isEmpty(favoriteColor)) {
                    Utils.showMessage(mSnackBar, "Select at least one color");
                } else {
                    Utils.copyToClipboard(mContext, favoriteColor);
                    Utils.putSharedPrefStringSetValue(this, Constant.COLOR_RECENT_LIST, favoriteColor);
                    Utils.showMessage(mSnackBar, "Copied " + favoriteColor + " to clipboard");
                }
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
                    ColorDetailDialogFragment colorDetailDialogFragment = new ColorDetailDialogFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("detailColor", fullInfoColor);
                    colorDetailDialogFragment.setArguments(bundle);
                    colorDetailDialogFragment.show(getSupportFragmentManager(), "detailDialog");
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_picker, menu);
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
            case android.R.id.home:
                intent = new Intent(mContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    private void back() {
        super.onBackPressed();
    }

    private void showErrorDialog() {
        SimpleDialog.Builder builder = new SimpleDialog.Builder(R.style.SimpleDialogLight) {
            @Override
            public void onPositiveActionClicked(DialogFragment fragment) {
                back();
                super.onPositiveActionClicked(fragment);
            }
        };

        (builder).message("Please check extension, existence of file or check internet connection if file on the cloud")
                .title("Error loading photo")
                .positiveAction("OK");

        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(getSupportFragmentManager(), null);
    }

}
