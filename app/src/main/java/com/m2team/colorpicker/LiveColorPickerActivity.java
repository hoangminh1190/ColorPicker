package com.m2team.colorpicker;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.m2team.colorpicker.livepicker.data.ColorItem;
import com.m2team.colorpicker.livepicker.data.ColorItems;
import com.m2team.colorpicker.livepicker.utils.Cameras;
import com.m2team.colorpicker.livepicker.views.CameraColorPickerPreview;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;

import java.util.ArrayList;


/**
 * An {@link AppCompatActivity} for picking colors by using the camera of the device.
 * <p/>
 * The user aims at a color with the camera of the device, when they click on the preview the color is selected.
 * An animation notifies the user of the selection.
 * <p/>
 * The last selected color can be saved by clicking the save button.
 * An animation notifies the user of the save.
 */
public class LiveColorPickerActivity extends AppCompatActivity implements CameraColorPickerPreview.OnColorSelectedListener, View.OnClickListener {

    /**
     * A tag used in the logs.
     */
    protected static final String TAG = LiveColorPickerActivity.class.getSimpleName();

    /**
     * The name of the property that animates the 'picked color'.
     * <p/>
     * Used by {@link LiveColorPickerActivity#mPickedColorProgressAnimator}.
     */
    protected static final String PICKED_COLOR_PROGRESS_PROPERTY_NAME = "pickedColorProgress";

    /**
     * The name of the property that animates the 'save completed'.
     * <p/>
     * Used by {@link LiveColorPickerActivity#mSaveCompletedProgressAnimator}.
     */
    protected static final String SAVE_COMPLETED_PROGRESS_PROPERTY_NAME = "saveCompletedProgress";

    /**
     * The duration of the animation of the confirm save message. (in millis).
     */
    protected static final long DURATION_CONFIRM_SAVE_MESSAGE = 400;

    /**
     * The delay before the confirm save message is hidden. (in millis).
     * <p/>
     * 1000 + DURATION_CONFIRM_SAVE_MESSAGE = 1400
     * The confirm save message should stay on screen for 1 second.
     */
    protected static final long DELAY_HIDE_CONFIRM_SAVE_MESSAGE = 1400;

    /**
     * A safe way to get an instance of the back {@link Camera}.
     */
    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return c;
    }

    /**
     * An instance of the {@link Camera} used for displaying the preview.
     */
    protected Camera mCamera;

    /**
     * A boolean for knowing the orientation of the activity.
     */
    protected boolean mIsPortrait;

    /**
     * A simple {@link FrameLayout} that contains the preview.
     */
    protected FrameLayout mPreviewContainer;

    /**
     * The {@link CameraColorPickerPreview} used for the preview.
     */
    protected CameraColorPickerPreview mCameraPreview;

    /**
     * A reference to the {@link LiveColorPickerActivity.CameraAsyncTask} that gets the {@link Camera}.
     */
    protected CameraAsyncTask mCameraAsyncTask;

    /**
     * The color selected by the user.
     * <p/>
     * The user "selects" a color by pointing a color with the camera.
     */
    protected int mSelectedColor;

    /**
     * The last picked color.
     * <p/>
     * The user "picks" a color by clicking the preview.
     */
    protected int mLastPickedColor;

    /**
     * A simple {@link View} used for showing the picked color.
     */
    protected View mPickedColorPreview;

    /**
     * A simple {@link View} used for animating the color being picked.
     */
    protected View mPickedColorPreviewAnimated;

    /**
     * An {@link ObjectAnimator} used for animating the color being picked.
     */
    protected ObjectAnimator mPickedColorProgressAnimator;

    /**
     * The delta for the translation on the x-axis of the mPickedColorPreviewAnimated.
     */
    protected float mTranslationDeltaX;

    /**
     * The delta for the translation on the y-axis of the mPickedColorPreviewAnimated.
     */
    protected float mTranslationDeltaY;

    /**
     * A simple {@link TextView} used for showing a human readable representation of the picked color.
     */
    protected TextView mColorPreviewText;

    /**
     * A simple {@link View} used for showing the selected color.
     */
    protected View mPointerRing;

    /**
     * An icon representing the "save completed" state.
     */
    protected View mSaveCompletedIcon;

    /**
     * The save button.
     */
    protected View mSaveButton;

    /**
     * A float representing the progress of the "save completed" state.
     */
    protected float mSaveCompletedProgress;

    /**
     * An {@link ObjectAnimator} used for animating the "save completed" state.
     */
    protected ObjectAnimator mSaveCompletedProgressAnimator;

    /**
     * A simple {@link TextView} that confirms the user that the color has been saved successfully.
     */
    protected TextView mConfirmSaveMessage;

    /**
     * An {@link Interpolator} used for showing the mConfirmSaveMessage.
     */
    protected Interpolator mConfirmSaveMessageInterpolator;

    /**
     * A {@link Runnable} that hide the confirm save message.
     * <p/>
     * This runnable is posted with some delayed on mConfirmSaveMessage each time a color is successfully saved.
     */
    protected Runnable mHideConfirmSaveMessage;

    /**
     * A simple boolean for keeping track of the device's camera flash state.
     */
    protected boolean mIsFlashOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_color_picker);

        initPickedColorProgressAnimator();
        initSaveCompletedProgressAnimator();
        initViews();
        initTranslationDeltas();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Setup the camera asynchronously.
        mCameraAsyncTask = new CameraAsyncTask();
        mCameraAsyncTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Cancel the Camera AsyncTask.
        mCameraAsyncTask.cancel(true);

        // Release the camera.
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }

        // Remove the camera preview
        if (mCameraPreview != null) {
            mPreviewContainer.removeView(mCameraPreview);
        }
    }

    @Override
    protected void onDestroy() {
        // Remove any pending mHideConfirmSaveMessage.
        mConfirmSaveMessage.removeCallbacks(mHideConfirmSaveMessage);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isFlashSupported()) {
            getMenuInflater().inflate(R.menu.menu_color_picker, menu);
            final MenuItem flashItem = menu.findItem(R.id.menu_color_picker_action_flash);
            int flashIcon = mIsFlashOn ? R.drawable.ic_action_flash_off : R.drawable.ic_action_flash_on;
            flashItem.setIcon(flashIcon);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        boolean handled;
        switch (itemId) {
            case android.R.id.home:
                finish();
                handled = true;
                break;

            case R.id.menu_color_picker_action_flash:
                toggleFlash();
                handled = true;
                break;

            case R.id.menu_color_picker_action_done:
                Intent intent = new Intent(this, MainSettingActivity.class);
                intent.putExtra("index", MainSettingActivity.Tab.RECENT_COLOR.ordinal());
                startActivity(intent);
                handled = true;
                break;

            default:
                handled = super.onOptionsItemSelected(item);
        }

        return handled;
    }

    @Override
    public void onColorSelected(int color) {
        mSelectedColor = color;
        mPointerRing.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void onClick(View v) {
        if (v == mCameraPreview) {
            animatePickedColor(mSelectedColor);
        } else if (v.getId() == R.id.activity_color_picker_save_button) {
            ColorItems.saveColorItem(this, new ColorItem(mLastPickedColor));
            setSaveCompleted(true);
            Utils.putSharedPrefStringSetValue(this, Constant.COLOR_RECENT_LIST, ColorItem.makeHexString(mLastPickedColor));
        }
    }

    /**
     * Initialize the views used in this activity.
     * <p/>
     * Internally find the view by their ids and set the click listeners.
     */

    protected void initViews() {
        mIsPortrait = getResources().getBoolean(R.bool.is_portrait);
        mPreviewContainer = (FrameLayout) findViewById(R.id.activity_color_picker_preview_container);
        mPickedColorPreview = findViewById(R.id.activity_color_picker_color_preview);
        mPickedColorPreviewAnimated = findViewById(R.id.activity_color_picker_animated_preview);
        mColorPreviewText = (TextView) findViewById(R.id.activity_color_picker_color_preview_text);
        mPointerRing = findViewById(R.id.activity_color_picker_pointer_ring);
        mSaveCompletedIcon = findViewById(R.id.activity_color_picker_save_completed);
        mSaveButton = findViewById(R.id.activity_color_picker_save_button);
        mSaveButton.setOnClickListener(this);
        mConfirmSaveMessage = (TextView) findViewById(R.id.activity_color_picker_confirm_save_message);
        mHideConfirmSaveMessage = new Runnable() {
            @Override
            public void run() {
                mConfirmSaveMessage.animate()
                        .translationY(-mConfirmSaveMessage.getMeasuredHeight())
                        .setDuration(DURATION_CONFIRM_SAVE_MESSAGE)
                        .start();
            }
        };
        positionConfirmSaveMessage();
        mConfirmSaveMessageInterpolator = new DecelerateInterpolator();

        mLastPickedColor = ColorItems.getLastPickedColor(this);
        applyPreviewColor(mLastPickedColor);
    }

    /**
     * Position mConfirmSaveMessage.
     * <p/>
     * Set the translationY of mConfirmSaveMessage to - mConfirmSaveMessage.getMeasuredHeight() so that it is correctly placed before the first animation.
     */
    protected void positionConfirmSaveMessage() {
        ViewTreeObserver vto = mConfirmSaveMessage.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    ViewTreeObserver vto = mConfirmSaveMessage.getViewTreeObserver();
                    vto.removeOnPreDrawListener(this);
                    mConfirmSaveMessage.setTranslationY(-mConfirmSaveMessage.getMeasuredHeight());
                    return true;
                }
            });
        }
    }

    /**
     * Initialize the deltas used for the translation of the preview of the picked color.
     */
    @SuppressLint("NewApi")
    protected void initTranslationDeltas() {
        ViewTreeObserver vto = mPointerRing.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver vto = mPointerRing.getViewTreeObserver();
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                        vto.removeGlobalOnLayoutListener(this);
                    } else {
                        vto.removeOnGlobalLayoutListener(this);
                    }

                    final Rect pointerRingRect = new Rect();
                    final Rect colorPreviewAnimatedRect = new Rect();
                    mPointerRing.getGlobalVisibleRect(pointerRingRect);
                    mPickedColorPreviewAnimated.getGlobalVisibleRect(colorPreviewAnimatedRect);

                    mTranslationDeltaX = pointerRingRect.left - colorPreviewAnimatedRect.left;
                    mTranslationDeltaY = pointerRingRect.top - colorPreviewAnimatedRect.top;
                }
            });
        }
    }


    /**
     * Initialize the animator used for the progress of the picked color.
     */
    protected void initPickedColorProgressAnimator() {
        mPickedColorProgressAnimator = ObjectAnimator.ofFloat(this, PICKED_COLOR_PROGRESS_PROPERTY_NAME, 1f, 0f);
        mPickedColorProgressAnimator.setDuration(400);
        mPickedColorProgressAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mPickedColorPreviewAnimated.setVisibility(View.VISIBLE);
                mPickedColorPreviewAnimated.getBackground().setColorFilter(mSelectedColor, PorterDuff.Mode.SRC_ATOP);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ColorItems.saveLastPickedColor(LiveColorPickerActivity.this, mLastPickedColor);
                applyPreviewColor(mLastPickedColor);
                mPickedColorPreviewAnimated.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mPickedColorPreviewAnimated.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * Check if the device's camera supports flash.
     *
     * @return Returns true if the device's camera supports flash, false otherwise.
     */
    protected boolean isFlashSupported() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    /**
     * Toggle the device's camera flash.
     * {@link LiveColorPickerActivity#isFlashSupported()} should be called before using this methods.
     */
    protected void toggleFlash() {
        if (mCamera != null) {
            final Camera.Parameters parameters = mCamera.getParameters();
            final String flashParameter = mIsFlashOn ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters.FLASH_MODE_TORCH;
            parameters.setFlashMode(flashParameter);
            mCamera.stopPreview();
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            mIsFlashOn = !mIsFlashOn;
            invalidateOptionsMenu();
        }
    }

    /**
     * Initialize the animator used for the progress of the "save completed" state.
     */
    protected void initSaveCompletedProgressAnimator() {
        mSaveCompletedProgressAnimator = ObjectAnimator.ofFloat(this, SAVE_COMPLETED_PROGRESS_PROPERTY_NAME, 1f, 0f);
    }

    /**
     * Apply the preview color.
     * <p/>
     * Display the preview color and its human representation.
     *
     * @param previewColor the preview color to apply.
     */
    protected void applyPreviewColor(int previewColor) {
        setSaveCompleted(false);
        mPickedColorPreview.getBackground().setColorFilter(previewColor, PorterDuff.Mode.SRC_ATOP);
        mColorPreviewText.setText(ColorItem.makeHexString(previewColor));
    }

    /**
     * Animate the color being picked.
     *
     * @param pickedColor the color being picked.
     */
    protected void animatePickedColor(int pickedColor) {
        mLastPickedColor = pickedColor;
        if (mPickedColorProgressAnimator.isRunning()) {
            mPickedColorProgressAnimator.cancel();
        }
        mPickedColorProgressAnimator.start();
    }

    /**
     * Set the "save completed" state.
     * <p/>
     * True means that the save is completed. The preview color should not be saved again.
     *
     * @param isSaveCompleted the "save completed" state.
     */
    protected void setSaveCompleted(boolean isSaveCompleted) {
        mSaveButton.setEnabled(!isSaveCompleted);
        mSaveCompletedProgressAnimator.cancel();
        mSaveCompletedProgressAnimator.setFloatValues(mSaveCompletedProgress, isSaveCompleted ? 0f : 1f);
        mSaveCompletedProgressAnimator.start();

        if (isSaveCompleted) {
            mConfirmSaveMessage.setVisibility(View.VISIBLE);
            mConfirmSaveMessage.animate().translationY(0).setDuration(DURATION_CONFIRM_SAVE_MESSAGE).setInterpolator(mConfirmSaveMessageInterpolator).start();
            mConfirmSaveMessage.removeCallbacks(mHideConfirmSaveMessage);
            mConfirmSaveMessage.postDelayed(mHideConfirmSaveMessage, DELAY_HIDE_CONFIRM_SAVE_MESSAGE);
        }
    }

    /**
     * Set the progress of the picked color animation.
     * <p/>
     * Used by {@link LiveColorPickerActivity#mPickedColorProgressAnimator}.
     *
     * @param progress A value in closed range [0,1] representing the progress of the picked color animation.
     */
    protected void setPickedColorProgress(float progress) {
        final float fastOppositeProgress = (float) Math.pow(1 - progress, 0.3f);
        final float translationX = (float) (mTranslationDeltaX * Math.pow(progress, 2f));
        final float translationY = mTranslationDeltaY * progress;

        mPickedColorPreviewAnimated.setTranslationX(translationX);
        mPickedColorPreviewAnimated.setTranslationY(translationY);
        mPickedColorPreviewAnimated.setScaleX(fastOppositeProgress);
        mPickedColorPreviewAnimated.setScaleY(fastOppositeProgress);
    }

    /**
     * Set the progress of the animation of the "save completed" state.
     * <p/>
     * Used by {@link LiveColorPickerActivity#mSaveCompletedProgressAnimator}.
     *
     * @param progress A value in closed range [0,1] representing the progress of the animation of the "save completed" state.
     */
    protected void setSaveCompletedProgress(float progress) {
        mSaveButton.setScaleX(progress);
        mSaveButton.setRotation(45 * (1 - progress));
        mSaveCompletedIcon.setScaleX(1 - progress);
        mSaveCompletedProgress = progress;
    }

    /**
     * Async task used to configure and start the camera preview.
     */
    private class CameraAsyncTask extends AsyncTask<Void, Void, Camera> {

        /**
         * The {@link android.view.ViewGroup.LayoutParams} used for adding the preview to its container.
         */
        protected FrameLayout.LayoutParams mPreviewParams;

        @Override
        protected Camera doInBackground(Void... params) {
            Camera camera = getCameraInstance();
            if (camera == null) {
                LiveColorPickerActivity.this.finish();
            } else {
                //configure Camera parameters
                Camera.Parameters cameraParameters = camera.getParameters();

                //get optimal camera preview size according to the layout used to display it
                Camera.Size bestSize = Cameras.getBestPreviewSize(
                        cameraParameters.getSupportedPreviewSizes()
                        , mPreviewContainer.getWidth()
                        , mPreviewContainer.getHeight()
                        , mIsPortrait);
                //set optimal camera preview
                cameraParameters.setPreviewSize(bestSize.width, bestSize.height);
                camera.setParameters(cameraParameters);

                //set camera orientation to match with current device orientation
                Cameras.setCameraDisplayOrientation(LiveColorPickerActivity.this, camera);

                //get proportional dimension for the layout used to display preview according to the preview size used
                int[] adaptedDimension = Cameras.getProportionalDimension(
                        bestSize
                        , mPreviewContainer.getWidth()
                        , mPreviewContainer.getHeight()
                        , mIsPortrait);

                //set up params for the layout used to display the preview
                mPreviewParams = new FrameLayout.LayoutParams(adaptedDimension[0], adaptedDimension[1]);
                mPreviewParams.gravity = Gravity.CENTER;
            }
            return camera;
        }

        @Override
        protected void onPostExecute(Camera camera) {
            super.onPostExecute(camera);

            // Check if the task is cancelled before trying to use the camera.
            if (!isCancelled()) {
                mCamera = camera;
                if (mCamera == null) {
                    LiveColorPickerActivity.this.finish();
                } else {
                    //set up camera preview
                    mCameraPreview = new CameraColorPickerPreview(LiveColorPickerActivity.this, mCamera);
                    mCameraPreview.setOnColorSelectedListener(LiveColorPickerActivity.this);
                    mCameraPreview.setOnClickListener(LiveColorPickerActivity.this);

                    //add camera preview
                    mPreviewContainer.addView(mCameraPreview, 0, mPreviewParams);
                }
            }
        }

        @Override
        protected void onCancelled(Camera camera) {
            super.onCancelled(camera);
            if (camera != null) {
                camera.release();
            }
        }
    }
}