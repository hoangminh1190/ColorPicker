package com.m2team.colorpicker.function;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.m2team.colorpicker.R;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;
import com.rey.material.widget.SnackBar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class CompareColorFragment extends Fragment implements View.OnClickListener {

    TextView tvHEX, tvRGB, tvHSV, tvHSL, tvCMYK, tvLAB, tvXYZ;
    TextView tvHEX1, tvRGB1, tvHSV1, tvHSL1, tvCMYK1, tvLAB1, tvXYZ1;
    LinearLayout ll1, ll2;
    SnackBar snackBar;
    String[] rgb, rgb1, hsv, hsv1, hsl, hsl1, cmyk, cmyk1, lab, lab1, xyz, xyz1;
    static Context context;

    private static LoadColor loadColor;

    private class LoadColor extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            fillValue(params[0], params[1]);
            return params;
        }

        @Override
        protected void onPostExecute(String[] colors) {
            super.onPostExecute(colors);
            int i = 0;
            while (i < ll1.getChildCount()) {
                if (ll1.getChildAt(i) != null)
                    ll1.getChildAt(i).setBackgroundColor(Color.parseColor(colors[0]));
                i++;
            }
            i = 0;
            while (i < ll2.getChildCount()) {
                if (ll2.getChildAt(i) != null)
                    ll2.getChildAt(i).setBackgroundColor(Color.parseColor(colors[1]));
                i++;
            }
            ll1.setBackgroundColor(Color.parseColor(colors[0]));
            ll2.setBackgroundColor(Color.parseColor(colors[1]));
            tvHEX.setText("HEX: " + colors[0]);
            tvRGB.setText("RGB: " + Utils.setStyleRGB(rgb));
            tvHSV.setText("HSV: " + Utils.setStyleHSV_HSL(hsv));
            tvHSL.setText("HSL: " + Utils.setStyleHSV_HSL(hsl));
            tvCMYK.setText("CMYK: " + Utils.setStyleCMYK(cmyk));
            tvLAB.setText("Lab: " + Utils.setStyleLab_XYZ(lab));
            tvXYZ.setText("XYZ: " + Utils.setStyleLab_XYZ(xyz));

            tvHEX1.setText("HEX: " + colors[1]);
            tvRGB1.setText("RGB: " + Utils.setStyleRGB(rgb1));
            tvHSV1.setText("HSV: " + Utils.setStyleHSV_HSL(hsv1));
            tvHSL1.setText("HSL: " + Utils.setStyleHSV_HSL(hsl1));
            tvCMYK1.setText("CMYK: " + Utils.setStyleCMYK(cmyk1));
            tvLAB1.setText("Lab: " + Utils.setStyleLab_XYZ(lab1));
            tvXYZ1.setText("XYZ: " + Utils.setStyleLab_XYZ(xyz1));
            Utils.putPrefValue(context, Constant.FIRST_COLOR_COMPARE, "");
            Utils.putPrefValue(context, Constant.SECOND_COLOR_COMPARE, "");
        }
    }

    public CompareColorFragment() {
    }

    public static CompareColorFragment newInstance() {
        CompareColorFragment fragment = new CompareColorFragment();
        return fragment;
    }

    private void init(View view) {
        ll1 = (LinearLayout) view.findViewById(R.id.ll1);
        ll2 = (LinearLayout) view.findViewById(R.id.ll2);
        tvHEX = (TextView) view.findViewById(R.id.hex);
        tvHEX1 = (TextView) view.findViewById(R.id.hex1);
        tvRGB = (TextView) view.findViewById(R.id.rgb);
        tvRGB1 = (TextView) view.findViewById(R.id.rgb1);
        tvHSV = (TextView) view.findViewById(R.id.hsv);
        tvHSV1 = (TextView) view.findViewById(R.id.hsv1);
        tvHSL = (TextView) view.findViewById(R.id.hsl);
        tvHSL1 = (TextView) view.findViewById(R.id.hsl1);
        tvCMYK = (TextView) view.findViewById(R.id.cmyk);
        tvCMYK1 = (TextView) view.findViewById(R.id.cmyk1);
        tvLAB = (TextView) view.findViewById(R.id.lab);
        tvLAB1 = (TextView) view.findViewById(R.id.lab1);
        tvXYZ = (TextView) view.findViewById(R.id.xyz);
        tvXYZ1 = (TextView) view.findViewById(R.id.xyz1);
        for (int i = 0; i < ll1.getChildCount(); i++) {
            View view1 = ll1.getChildAt(i);
            view1.setOnClickListener(this);
            View view2 = ll2.getChildAt(i);
            view2.setOnClickListener(this);
        }
    }

    private void fillValue(String color, String color1) {
        String fullInfoColor1 = Utils.setFullInfoColor(Color.parseColor(color));
        rgb = Utils.getOneColorMode(fullInfoColor1, 1).split(Constant.DOLLAR_TOKEN);
        hsv = Utils.getOneColorMode(fullInfoColor1, 2).split(Constant.DOLLAR_TOKEN);
        hsl = Utils.getOneColorMode(fullInfoColor1, 3).split(Constant.DOLLAR_TOKEN);
        cmyk = Utils.getOneColorMode(fullInfoColor1, 4).split(Constant.DOLLAR_TOKEN);
        lab = Utils.getOneColorMode(fullInfoColor1, 5).split(Constant.DOLLAR_TOKEN);
        xyz = Utils.getOneColorMode(fullInfoColor1, 6).split(Constant.DOLLAR_TOKEN);

        String fullInfoColor11 = Utils.setFullInfoColor(Color.parseColor(color1));
        rgb1 = Utils.getOneColorMode(fullInfoColor11, 1).split(Constant.DOLLAR_TOKEN);
        hsv1 = Utils.getOneColorMode(fullInfoColor11, 2).split(Constant.DOLLAR_TOKEN);
        hsl1 = Utils.getOneColorMode(fullInfoColor11, 3).split(Constant.DOLLAR_TOKEN);
        cmyk1 = Utils.getOneColorMode(fullInfoColor11, 4).split(Constant.DOLLAR_TOKEN);
        lab1 = Utils.getOneColorMode(fullInfoColor11, 5).split(Constant.DOLLAR_TOKEN);
        xyz1 = Utils.getOneColorMode(fullInfoColor11, 6).split(Constant.DOLLAR_TOKEN);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (context == null) context = getActivity();
        View view = inflater.inflate(R.layout.fragment_compare_color, container, false);
        init(view);
        snackBar = (SnackBar) view.findViewById(R.id.snackbar);
        snackBar.setVisibility(View.INVISIBLE);
        String firstColor = Utils.getPrefString(context, Constant.FIRST_COLOR_COMPARE);
        String secondColor = Utils.getPrefString(context, Constant.SECOND_COLOR_COMPARE);

        if (TextUtils.isEmpty(firstColor) && TextUtils.isEmpty(secondColor)) {
            snackBar.setVisibility(View.VISIBLE);
            Utils.showMessage(snackBar, "You must choose two colors to compare");
        } else if ((TextUtils.isEmpty(firstColor) && !TextUtils.isEmpty(secondColor)) || (!TextUtils.isEmpty(firstColor) && TextUtils.isEmpty(secondColor))) {
            snackBar.setVisibility(View.VISIBLE);
            Utils.showMessage(snackBar, "You must choose one more color to compare");
        } else {
            loadColor = new LoadColor();
            loadColor.execute(firstColor, secondColor);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            TextView textView = (TextView) v;
            CharSequence text = textView.getText();
            if (!TextUtils.isEmpty(text)) {
                String s = text.toString();
                s = s.substring(s.lastIndexOf(":") + 1).trim();
                Utils.showMessage(snackBar, "Copied " + s + " to clipboard");
            }

        }
    }
}
