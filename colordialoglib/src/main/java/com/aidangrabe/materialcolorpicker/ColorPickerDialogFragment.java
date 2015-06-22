package com.aidangrabe.materialcolorpicker;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.aidangrabe.materialcolorpicker.views.ColorCircleView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by aidan on 23/04/15.
 * ColorPicker Dialog that allows the user to select a colour from the Material
 * Design Colour Palette: http://www.google.com/design/spec/style/color.html#color-color-palette
 */
public class ColorPickerDialogFragment extends DialogFragment {

    private static final int CIRCLES_PER_ROW = 4;
    private static final int NUM_ROWS = 5;
    private List<ColorCircleView> mColorViews;
    private Map<Integer, Integer> mColorMap;
    private ColorSelectedListener mListener;

    public interface ColorSelectedListener {
        void onColorSelected(int color);
    }

    // the click listener for when a color is selected
    private final View.OnTouchListener mColorOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            // act like an onClick listener, but with a MotionEvent
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                ColorCircleView colorView = (ColorCircleView) view;
                final int arrayId = mColorMap.get(colorView.getColor());

                // if no sub array is found, return the colour
                if (arrayId == 0) {

                    dismiss();

                    if (mListener != null) {
                        mListener.onColorSelected(colorView.getColor());
                    }

                }
                // if a sub array is found, load up the colors from that array
                else {

                    // TODO: animate

                    setColorArray(arrayId);

                }

            }

            return false;

        }

    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.color_palette, container, false);

        mColorViews = new ArrayList<>();
        mColorMap = new HashMap<>();

        // the size of the color views
        int size =  (int) dp2px(48);
        LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(size, size);
        colorParams.setMargins(5, 5, 5, 5);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        // create the rows
        for (int i = 0; i < NUM_ROWS; i++) {
            LinearLayout rowLayout = new LinearLayout(getActivity());
            rowLayout.setLayoutParams(rowParams);
            rowLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);

            // create the colours within the rows
            for (int j = 0; j < CIRCLES_PER_ROW; j++) {
                ColorCircleView colorView = new ColorCircleView(getActivity());
                colorView.setLayoutParams(colorParams);
                colorView.setOnTouchListener(mColorOnTouchListener);
                rowLayout.addView(colorView);
                mColorViews.add(colorView);
            }
            view.addView(rowLayout);
        }

        setColorArray(R.array.primary_colors);

        return view;

    }

    public void setColorArray(@ArrayRes int colorArray) {

        mColorMap = parseColorArray(colorArray);
        setColors(new ArrayList<>(mColorMap.keySet()));

    }

    public void setColors(List<Integer> colors) {

        int i = 0;
        // change the color of the views
        for (int color : colors) {
            ColorCircleView colorView = mColorViews.get(i);
            colorView.setColor(color);
            colorView.setVisibility(View.VISIBLE);
            i++;
        }

        // hide any extra views
        for (int j = i; j < mColorViews.size(); j++) {
            mColorViews.get(j).setVisibility(View.GONE);
        }

    }

    /**
     * Parse the given array of colours into an ordered map. The order of the
     * array is maintained. The key of the map is the colour as an integer,
     * and the value is the id of the next array of colours to display, 0 if
     * no array should come next
     * @param colorArray the id of the array of colours to parse
     * @return an ordered map of colour->next_array_id
     */
    private LinkedHashMap<Integer, Integer> parseColorArray(@ArrayRes int colorArray) {
        LinkedHashMap<Integer, Integer> colorMap = new LinkedHashMap<>();

        String[] hexColors = getResources().getStringArray(colorArray);
        int[] colors = new int[hexColors.length];

        for (int i = 0; i < hexColors.length; i++) {
            String[] parts = hexColors[i].split("\\|", 2);
            String hexValue = parts[0];
            int arrayId = 0;
            colors[i] = Color.parseColor(hexValue);
            if (parts.length > 1) {
                arrayId = getResId(parts[1], R.array.class);
            }
            colorMap.put(colors[i], arrayId);
        }

        return colorMap;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Select a color");

        View view = dialog.getWindow().getDecorView();

        if (view != null) {
            addRevealAnimationToView(view, 0, 0);
        }

        return dialog;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addRevealAnimationToView(View view, final int centerX, final int centerY) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // if the dialog is null, we are showing the dialog
            // if the dialog is not null, we should dismiss it
            if (getDialog() != null) {
                getDialog().dismiss();
            }
            return;
        }

        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                final float startRadius, endRadius;
                startRadius = 0;
                endRadius = v.getWidth();

                v.removeOnLayoutChangeListener(this);
                Animator reveal = ViewAnimationUtils.createCircularReveal(v, centerX, centerY, startRadius, endRadius);
                reveal.setDuration(300);
                reveal.start();
            }
        });
    }

    /**
     * Convert dp to pixels
     * @param dp the dp value to convert
     * @return the corresponding number of pixels
     */
    private float dp2px(float dp) {

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);

    }

    /**
     * Get the resource identifier of a given resource name from the R file
     * @param resName the name of the resource
     * @param c the R class to lookup the resource on eg. R.drawable.class|R.string.class
     * @return the id of the given resource or 0
     */
    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void setOnColorSelectedListener(ColorSelectedListener listener) {
        mListener = listener;
    }

}
