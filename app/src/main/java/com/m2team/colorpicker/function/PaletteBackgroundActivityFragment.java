package com.m2team.colorpicker.function;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.aidangrabe.materialcolorpicker.ColorPickerDialogFragment;
import com.m2team.colorpicker.PaletteBackgroundActivity;
import com.m2team.colorpicker.R;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;
import com.rey.material.widget.Button;
import com.rey.material.widget.Spinner;

/**
 * A placeholder fragment containing a simple view.
 */
public class PaletteBackgroundActivityFragment extends Fragment implements Spinner.OnItemClickListener {
    private LinearLayout parentView;
    private EditText tvPercent;
    private View colorView;

    public PaletteBackgroundActivityFragment() {
    }

    public static PaletteBackgroundActivityFragment newInstance() {
        return new PaletteBackgroundActivityFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parentView = (LinearLayout) inflater.inflate(R.layout.fragment_palette, container, false);
        Spinner spn_label = (Spinner) parentView.findViewById(R.id.spinner_number_colors);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.row_spn, new String[]{"5", "4", "3", "2", "1"});
        adapter.setDropDownViewResource(R.layout.row_spn_dropdown);
        spn_label.setAdapter(adapter);
        spn_label.setOnItemClickListener(this);
        spn_label.setSelection(0);
        generateLayout(5);
        return parentView;
    }

    @Override
    public boolean onItemClick(Spinner parent, View view, int position, long id) {
        if (parentView.getChildCount() > 1)
            do {
                parentView.removeViewAt(1);
            } while (parentView.getChildCount() > 1);
        TextView tv = (TextView) view;
        Toast.makeText(getActivity(), tv.getText(), Toast.LENGTH_SHORT).show();
        int numberOfColor = Integer.parseInt(tv.getText().toString());
        generateLayout(numberOfColor);
        parent.setSelection(position);
        return false;
    }

    private void generateLayout(final int numberOfColor) {
        if (numberOfColor > 0) {
            LinearLayout rowLayout;
            final int percent = 100 / numberOfColor;
            int size = (int) Utils.dp2px(getActivity(), 50);
            final LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            colorParams.setMargins(0, 0, 0, 0);

            LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(size, ViewGroup.LayoutParams.MATCH_PARENT);
            textViewParams.setMargins(0, 0, 0, 0);
            textViewParams.gravity = Gravity.CENTER;

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    size);
            rowParams.setMargins(0, 10, 0, 10);

            ViewGroup.LayoutParams buttonParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size);
            // create the rows
            for (int i = 0; i < numberOfColor; i++) {
                rowLayout = new LinearLayout(getActivity());
                rowLayout.setLayoutParams(rowParams);
                rowLayout.setGravity(Gravity.CENTER_VERTICAL);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setTag(i);
                // create the colours within the rows
                colorView = new View(getActivity());
                int defaultColor = Color.WHITE;
                colorView.setBackgroundColor(defaultColor);
                colorView.setLayoutParams(colorParams);
                colorView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        ColorPickerDialogFragment dialogFragment = new ColorPickerDialogFragment();
                        dialogFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.ColorSelectedListener() {
                            @Override
                            public void onColorSelected(int color) {
                                v.setBackgroundColor(color);
                            }
                        });
                        dialogFragment.show(getFragmentManager(), "paletteDialog");
                    }
                });

                tvPercent = new EditText(getActivity());
                tvPercent.setLayoutParams(textViewParams);
                tvPercent.setMaxLines(1);
                tvPercent.setTextSize(16);
                tvPercent.setText(String.valueOf(percent));
                tvPercent.setGravity(Gravity.CENTER);
                tvPercent.setInputType(EditorInfo.TYPE_CLASS_NUMBER);

                rowLayout.addView(tvPercent);
                rowLayout.addView(colorView);

                parentView.addView(rowLayout);
            }
            Button btnGenerate = new Button(getActivity());
            btnGenerate.setLayoutParams(buttonParams);
            btnGenerate.setText("GENERATE");
            btnGenerate.applyStyle(R.style.FlatWaveButtonRippleStyle);
            btnGenerate.setGravity(Gravity.CENTER);
            btnGenerate.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), PaletteBackgroundActivity.class);
                            intent.putExtra("numberOfColors", numberOfColor);
                            String colorPercent;
                            ColorDrawable colorDrawable;
                            int eachPercent = percent, i = 0, j = 0;
                            do {
                                View view = parentView.getChildAt(i);
                                if (view instanceof LinearLayout) {
                                    LinearLayout layout = (LinearLayout) view;
                                    tvPercent = (EditText) layout.getChildAt(0);
                                    colorView = layout.getChildAt(1);
                                    colorDrawable = (ColorDrawable) colorView.getBackground();

                                    if (!TextUtils.isEmpty(tvPercent.getText().toString())) {
                                        eachPercent = Integer.parseInt(tvPercent.getText().toString());
                                    }
                                    colorPercent = eachPercent + Constant.VAR_TOKEN + colorDrawable.getColor();
                                    intent.putExtra("colorPercent" + j, colorPercent);
                                    j++;
                                }
                                i++;
                            }
                            while (i <= parentView.getChildCount());
                            startActivity(intent);
                        }
                    }

            );
            parentView.addView(btnGenerate);
            parentView.invalidate();
        }
    }

}
