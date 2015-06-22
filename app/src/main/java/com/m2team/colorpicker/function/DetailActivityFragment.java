package com.m2team.colorpicker.function;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.m2team.colorpicker.R;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;
import com.rey.material.widget.SnackBar;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements View.OnClickListener {
    static String detailColor;
    TextView tvHEX, tvRGB, tvHSV, tvHSL, tvCMYK, tvLAB, tvXYZ;
    LinearLayout ll1;
    View viewBG;
    SnackBar snackBar;

    public DetailActivityFragment() {
    }

    public static DetailActivityFragment newInstance(String detailColor) {
        DetailActivityFragment fragment = new DetailActivityFragment();
        DetailActivityFragment.detailColor = detailColor;
        return fragment;
    }

    private void init(View view) {
        ll1 = (LinearLayout) view.findViewById(R.id.ll1);
        viewBG = view.findViewById(R.id.detail_color);
        tvHEX = (TextView) view.findViewById(R.id.tv_hex);
        tvRGB = (TextView) view.findViewById(R.id.tv_rgb);
        tvHSV = (TextView) view.findViewById(R.id.tv_hsv);
        tvHSL = (TextView) view.findViewById(R.id.tv_hsl);
        tvCMYK = (TextView) view.findViewById(R.id.tv_cmyk);
        tvLAB = (TextView) view.findViewById(R.id.tv_lab);
        tvXYZ = (TextView) view.findViewById(R.id.tv_xyz);
        snackBar = (SnackBar) view.findViewById(R.id.snackbar);
        snackBar.setVisibility(View.INVISIBLE);
        for (int i = 0; i < ll1.getChildCount(); i++) {
            View view1 = ll1.getChildAt(i);
            view1.setOnClickListener(this);
        }
    }

    private void fillValue(String color) {
        String hex = Utils.getOneColorMode(color, 0);
        String[] rgb = Utils.getOneColorMode(color,1).split(Constant.DOLLAR_TOKEN);
        String[] hsv = Utils.getOneColorMode(color,2).split(Constant.DOLLAR_TOKEN);
        String[] hsl = Utils.getOneColorMode(color,3).split(Constant.DOLLAR_TOKEN);
        String[] cmyk = Utils.getOneColorMode(color,4).split(Constant.DOLLAR_TOKEN);
        String[] lab = Utils.getOneColorMode(color,5).split(Constant.DOLLAR_TOKEN);
        String[] xyz = Utils.getOneColorMode(color,6).split(Constant.DOLLAR_TOKEN);
        //Utils.getFullInfoColor(hex, Utils.getColorRgb(color), Utils.getColorHsv(color), Utils.getColorHsl(color), Utils.getColorCMYK(color), Utils.getColorLab(color), Utils.getColorXYZ(color));
        viewBG.setBackgroundColor(Color.parseColor(hex));
        tvHEX.setText("HEX: " + hex);
        tvRGB.setText("RGB: " + Utils.setStyleRGB(rgb));
        tvHSV.setText("HSV: " + Utils.setStyleHSV_HSL(hsv));
        tvHSL.setText("HSL: " + Utils.setStyleHSV_HSL(hsl));
        tvCMYK.setText("CMYK: " + Utils.setStyleCMYK(cmyk));
        tvLAB.setText("Lab: " + Utils.setStyleLab_XYZ(lab));
        tvXYZ.setText("XYZ: " + Utils.setStyleLab_XYZ(xyz));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_color, container, false);
        init(view);
        if (!TextUtils.isEmpty(detailColor)) {
            fillValue(detailColor);
        } else {
            snackBar.setVisibility(View.VISIBLE);
            Utils.showMessage(snackBar, "Select one color to view");
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            TextView textView = (TextView) v;
            String s = textView.getText().toString();
            if (!TextUtils.isEmpty(s)) {
                Utils.copyToClipboard(getActivity(), s);
                Utils.putSharedPrefStringSetValue(getActivity(), Constant.COLOR_RECENT_LIST, Utils.getOneColorMode(detailColor, 0));
                Utils.showMessage(snackBar, "Copied " + s + " to clipboard");
            }
        }
    }
}
