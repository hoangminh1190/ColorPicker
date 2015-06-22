package com.m2team.colorpicker.function;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.m2team.colorpicker.R;
import com.m2team.colorpicker.utils.ColorSpaceConverter;
import com.m2team.colorpicker.utils.Utils;
import com.rey.material.widget.Button;
import com.rey.material.widget.EditText;
import com.rey.material.widget.SnackBar;
import com.rey.material.widget.Spinner;

/**
 * A placeholder fragment containing a simple view.
 */
public class ConvertColorFragment extends Fragment implements View.OnClickListener {

    Spinner spinnerLeft, spinnerRight;
    EditText edtLeft, edtRight;
    SnackBar snackBar;
    Button btnConvert;
    ColorSpaceConverter converter = new ColorSpaceConverter();

    public ConvertColorFragment() {
    }

    public static ConvertColorFragment newInstance() {
        ConvertColorFragment fragment = new ConvertColorFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_convert, container, false);
        spinnerLeft = (Spinner) view.findViewById(R.id.spinner_left_mode);
        spinnerRight = (Spinner) view.findViewById(R.id.spinner_right_mode);
        edtLeft = (EditText) view.findViewById(R.id.edt_left);
        edtRight = (EditText) view.findViewById(R.id.edt_right);
        snackBar = (SnackBar) view.findViewById(R.id.snackbar);
        btnConvert = (Button) view.findViewById(R.id.btn_convert);
        btnConvert.setOnClickListener(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.row_spn, getResources().getStringArray(R.array.color_mode_convert));
        adapter.setDropDownViewResource(R.layout.row_spn_dropdown);

        spinnerLeft.setAdapter(adapter);
        spinnerLeft.setSelection(0);
        String text;
        if (!TextUtils.isEmpty(text = Utils.getFromClipboard(getActivity()))) {
            if (text.contains("#")) {
                text = text.substring(text.lastIndexOf("#"), text.length());
            }
            edtLeft.setText(text);
        }
        spinnerRight.setAdapter(adapter);
        spinnerRight.setSelection(1);
        spinnerLeft.setOnItemClickListener(new Spinner.OnItemClickListener() {
            @Override
            public boolean onItemClick(Spinner parent, View view, int position, long id) {
                if (position == 3) {
                    Utils.showMessage(snackBar, "Do not support this color mode currently");
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_convert:
                int leftPos = spinnerLeft.getSelectedItemPosition();
                int rightPos = spinnerRight.getSelectedItemPosition();
                String value = edtLeft.getText().toString();
                if (TextUtils.isEmpty(value)) {
                    snackBar.setVisibility(View.VISIBLE);
                    Utils.showMessage(snackBar, "Input color value to convert");
                    return;
                } else {
                    value = value.trim();
                    try {
                        if (leftPos == rightPos) {
                            edtRight.setText(value);
                            return;
                        }
                        switch (leftPos) {
                            case 0://HEX
                                int[] rgb = converter.hexToRGB(value);
                                switch (rightPos) {
                                    case 1:
                                        String s = rgb[0] + " " + rgb[1] + " " + rgb[2];
                                        setResult(s);
                                        break;
                                    case 2:
                                        float[] hsv = new float[3];
                                        Color.RGBToHSV(rgb[0], rgb[1], rgb[2], hsv);
                                        setResult(Utils.setStyleHSV_HSL(hsv));
                                        break;
                                    case 3:
                                        float[] cmyk = converter.rgbToCmyk(new float[]{rgb[0], rgb[1], rgb[2]});
                                        setResult(Utils.setStyleCMYK(cmyk));
                                        break;
                                }
                                break;
                            case 1://RGB
                                String[] str = value.split(" ");
                                if (str.length != 3) {
                                    setResult(null);
                                    return;
                                }
                                int r = Integer.parseInt(str[0].trim());
                                int g = Integer.parseInt(str[1].trim());
                                int b = Integer.parseInt(str[2].trim());
                                switch (rightPos) {
                                    case 0://rgb to hex
                                        setResult(converter.rgbToHex(r, g, b));
                                        break;
                                    case 2://rgb to hsv
                                        float[] hsv = new float[3];
                                        Color.RGBToHSV(r, g, b, hsv);
                                        setResult(Utils.setStyleHSV_HSL(hsv));
                                        break;
                                    case 3://rgb to cmyk
                                        setResult(Utils.setStyleCMYK(converter.rgbToCmyk(new float[]{r, g, b})));
                                        break;
                                }
                                break;
                            case 2://HSV
                                str = value.split(" ");
                                if (str.length != 3) {
                                    setResult(null);
                                    return;
                                }
                                float hue = Float.parseFloat(str[0].trim());
                                float saturation = Float.parseFloat(str[1].trim());
                                float values = Float.parseFloat(str[2].trim());
                                switch (rightPos) {
                                    case 0://hsv to hex
                                        int color = Color.HSVToColor(new float[]{hue, saturation, values});
                                        setResult(converter.rgbToHex(Color.red(color), Color.green(color), Color.blue(color)));
                                        break;
                                    case 1://hsv to rgb
                                        color = Color.HSVToColor(new float[]{hue, saturation, values});
                                        setResult(Utils.setStyleRGB(new int[]{Color.red(color), Color.green(color), Color.blue(color)}));
                                        break;
                                    case 3://hsv to cmyk
                                        color = Color.HSVToColor(new float[]{hue, saturation, values});
                                        setResult(Utils.setStyleCMYK(converter.rgbToCmyk(new float[]{Color.red(color), Color.green(color), Color.blue(color)})));
                                        break;
                                }
                                break;
                            case 3: //CMYK
                                Utils.showMessage(snackBar, "Do not support this color mode currently");
                                return;
                        }


                    } catch (Exception e) {
                        setResult(null);
                    }
                }
                break;
        }
    }

    private void setResult(String result) {
        if (!TextUtils.isEmpty(result)) {
            edtRight.setText(result);
        } else {
            Utils.showMessage(snackBar, "Cannot convert color value. Check input value again");
        }
    }
}
