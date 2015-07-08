package com.m2team.colorpicker.function;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.aidangrabe.materialcolorpicker.views.ColorCircleView;
import com.m2team.colorpicker.utils.ColorSpaceConverter;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;
import java.util.ArrayList;

public class ColorDetectDialogFragment extends DialogFragment implements View.OnClickListener {

    private static final int CIRCLES_PER_ROW = 5;

    public ColorDetectDialogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        LinearLayout view = (LinearLayout) inflater.inflate(com.aidangrabe.materialcolorpicker.R.layout.color_palette, container, false);

        Bundle bundle = getArguments();
        ArrayList<Integer> swatches = new ArrayList<>();
        if (bundle != null) {
            swatches = bundle.getIntegerArrayList("detectColors");
        }
        double rows = Math.ceil(swatches.size() / CIRCLES_PER_ROW);
        int index = 0;
        // the size of the color views
        int size = (int) dp2px();
        LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(size, size);
        colorParams.setMargins(5, 5, 5, 5);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        // create the rows
        for (int i = 0; i < rows; i++) {
            if (index == swatches.size()) break;
            LinearLayout rowLayout = new LinearLayout(getActivity());
            rowLayout.setLayoutParams(rowParams);
            rowLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);

            // create the colours within the rows
            for (int j = 0; j < CIRCLES_PER_ROW; j++) {
                ColorCircleView colorView = new ColorCircleView(getActivity());
                colorView.setLayoutParams(colorParams);
                colorView.setColor(swatches.get(index));
                colorView.setVisibility(View.VISIBLE);
                colorView.setOnClickListener(this);
                rowLayout.addView(colorView);
                index++;
            }
            view.addView(rowLayout);
        }
        return view;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Detection colors");

        View view = dialog.getWindow().getDecorView();

        if (view != null) {
            addRevealAnimationToView(view);
        }

        return dialog;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addRevealAnimationToView(View view) {
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
                Animator reveal = ViewAnimationUtils.createCircularReveal(v, 0, 0, startRadius, endRadius);
                reveal.setDuration(300);
                reveal.start();
            }
        });
    }

    private float dp2px() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 48, displayMetrics);
    }

    @Override
    public void onClick(View v) {
        ColorCircleView view = (ColorCircleView) v;
        int color = view.getColor();
        String favoriteColor = new ColorSpaceConverter().rgbToHex(Color.red(color), Color.green(color), Color.blue(color));
        Utils.copyToClipboard(getActivity(), favoriteColor);
        Utils.putSharedPrefStringSetValue(getActivity(), Constant.COLOR_RECENT_LIST, favoriteColor);
        Toast.makeText(getActivity(), "Copied " + favoriteColor + " to clipboard", Toast.LENGTH_SHORT).show();
    }
}
