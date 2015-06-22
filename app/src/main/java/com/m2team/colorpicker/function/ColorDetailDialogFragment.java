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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.m2team.colorpicker.R;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;
import com.rey.material.widget.Button;
import com.rey.material.widget.SnackBar;

public class ColorDetailDialogFragment extends DialogFragment implements View.OnClickListener {

    TextView tvHEX, tvRGB, tvHSV, tvHSL, tvCMYK, tvLAB, tvXYZ;
    LinearLayout ll1;
    Button btnClose;
    View viewBG;
    String detailColor;

    public ColorDetailDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            detailColor = bundle.getString("detailColor");
        }
        if (getDialog() != null && getDialog().getWindow() != null)
            getDialog().getWindow().getAttributes().windowAnimations = R.style.SimpleDialogLight;
        //setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.layout_dialog_color_detail, container, false);
        init(view);
        fillValue(detailColor);
        return view;
    }


    private void init(View view) {
        ll1 = (LinearLayout) view.findViewById(R.id.ll1);
        viewBG = view.findViewById(R.id.detail_color);
        btnClose = (Button) view.findViewById(R.id.btn_close);
        tvHEX = (TextView) view.findViewById(R.id.tv_hex);
        tvRGB = (TextView) view.findViewById(R.id.tv_rgb);
        tvHSV = (TextView) view.findViewById(R.id.tv_hsv);
        tvHSL = (TextView) view.findViewById(R.id.tv_hsl);
        tvCMYK = (TextView) view.findViewById(R.id.tv_cmyk);
        tvLAB = (TextView) view.findViewById(R.id.tv_lab);
        tvXYZ = (TextView) view.findViewById(R.id.tv_xyz);

        for (int i = 0; i < ll1.getChildCount(); i++) {
            View view1 = ll1.getChildAt(i);
            view1.setOnClickListener(this);
        }
        btnClose.setOnClickListener(this);
    }

    private void fillValue(String color) {
        String hex = Utils.getOneColorMode(color, 0);
        String[] rgb = Utils.getOneColorMode(color, 1).split(Constant.DOLLAR_TOKEN);
        String[] hsv = Utils.getOneColorMode(color, 2).split(Constant.DOLLAR_TOKEN);
        String[] hsl = Utils.getOneColorMode(color, 3).split(Constant.DOLLAR_TOKEN);
        String[] cmyk = Utils.getOneColorMode(color, 4).split(Constant.DOLLAR_TOKEN);
        String[] lab = Utils.getOneColorMode(color, 5).split(Constant.DOLLAR_TOKEN);
        String[] xyz = Utils.getOneColorMode(color, 6).split(Constant.DOLLAR_TOKEN);
        viewBG.setBackgroundColor(Color.parseColor(hex));
        tvHEX.setText("HEX:\t\t\t" + hex);
        tvRGB.setText("RGB:\t\t\t" + Utils.setStyleRGB(rgb));
        tvHSV.setText("HSV:\t\t\t" + Utils.setStyleHSV_HSL(hsv));
        tvHSL.setText("HSL:\t\t\t" + Utils.setStyleHSV_HSL(hsl));
        tvCMYK.setText("CMYK:\t" + Utils.setStyleCMYK(cmyk));
        tvLAB.setText("Lab:\t\t\t\t" + Utils.setStyleLab_XYZ(lab));
        tvXYZ.setText("XYZ:\t\t\t" + Utils.setStyleLab_XYZ(xyz));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Utils.setColorDialog(dialog, Utils.getOneColorMode(detailColor, 0).toUpperCase(), Color.parseColor(Utils.getOneColorMode(detailColor, 0)));
        /*View view = dialog.getWindow().getDecorView();

        if (view != null) {
            addRevealAnimationToView(view, 0, 0);
        }*/

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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_close) {
            if (getDialog() != null) getDialog().dismiss();
        } else if (v instanceof TextView) {
            TextView textView = (TextView) v;
            CharSequence text = textView.getText();
            if (!TextUtils.isEmpty(text)) {
                String s = text.toString();
                s = s.substring(s.lastIndexOf(":") + 1).replace("\t", "").trim();
                Utils.copyToClipboard(getActivity(), s);
                Utils.putSharedPrefStringSetValue(getActivity(), Constant.COLOR_RECENT_LIST, Utils.getOneColorMode(detailColor, 0));
                Toast.makeText(getActivity(), "Copied " + s + " to clipboard", Toast.LENGTH_SHORT).show();
            }
        }

    }

}
