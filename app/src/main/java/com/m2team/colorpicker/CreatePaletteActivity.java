package com.m2team.colorpicker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aidangrabe.materialcolorpicker.ColorPickerDialogFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.Button;
import com.rey.material.widget.Spinner;


public class CreatePaletteActivity extends AppCompatActivity implements Spinner.OnItemClickListener {
    private LinearLayout parentView;
    private int numberOfColor;
    private EditText tvPercent;
    private View colorView;
    private RadioButton radioHoz;

    private void init() {
        parentView = (LinearLayout) findViewById(R.id.ll1);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioHoz = (RadioButton) findViewById(R.id.radioHoz);
        RadioButton radioVer = (RadioButton) findViewById(R.id.radioVer);
        radioHoz.setChecked(true);
        Spinner spn_label = (Spinner) findViewById(R.id.spinner_number_colors);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_spn, new String[]{"5", "4", "3", "2", "1"});
        adapter.setDropDownViewResource(R.layout.row_spn_dropdown);
        spn_label.setAdapter(adapter);
        spn_label.setOnItemClickListener(this);
        spn_label.setSelection(0);
        spn_label.setFocusableInTouchMode(true);
        spn_label.requestFocus();
        numberOfColor = 5;

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest.Builder builder = new AdRequest.Builder();
        builder.addTestDevice(getString(R.string.test_device_id_n910f));
        builder.addTestDevice(getString(R.string.test_device_id_htc));
        AdRequest adRequest = builder.build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_palette);
        init();
        generateLayout();
    }

    @Override
    public boolean onItemClick(Spinner parent, View view, int position, long id) {
        if (parentView.getChildCount() > 0)
            do {
                parentView.removeViewAt(0);
            } while (parentView.getChildCount() > 0);
        TextView tv = (TextView) view;
        numberOfColor = Integer.parseInt(tv.getText().toString());
        generateLayout();
        parent.setSelection(position);
        return false;
    }

    private void generateLayout() {
        if (numberOfColor > 0) {
            LinearLayout rowLayout;
            final int percent = 100 / numberOfColor;
            int size = (int) Utils.dp2px(this, 50);
            final LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            colorParams.setMargins(0, 0, 0, 0);

            LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams((int) Utils.dp2px(this, 80), ViewGroup.LayoutParams.MATCH_PARENT);
            textViewParams.setMargins(0, 0, 0, 0);
            textViewParams.gravity = Gravity.CENTER;

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    size);
            rowParams.setMargins(0, 10, 0, 10);

            ViewGroup.LayoutParams buttonParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size);

            // create the rows
            for (int i = 0; i < numberOfColor; i++) {
                rowLayout = new LinearLayout(this);
                rowLayout.setLayoutParams(rowParams);
                rowLayout.setGravity(Gravity.CENTER_VERTICAL);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setTag(i);
                // create the colours within the rows
                colorView = new View(this);
                int defaultColor = Color.WHITE;
                colorView.setTag(defaultColor);
                colorView.setBackgroundResource(R.drawable.border_textview);
                colorView.setLayoutParams(colorParams);
                colorView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        ColorPickerDialogFragment dialogFragment = new ColorPickerDialogFragment();
                        dialogFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.ColorSelectedListener() {
                            @Override
                            public void onColorSelected(int color) {
                                v.setBackgroundColor(color);
                                v.setTag(color);
                            }
                        });
                        dialogFragment.show(getSupportFragmentManager(), "paletteDialog");
                    }
                });

                tvPercent = new EditText(this);
                tvPercent.setSelectAllOnFocus(true);
                tvPercent.setLayoutParams(textViewParams);
                tvPercent.setMaxLines(1);
                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter.LengthFilter(2);
                tvPercent.setFilters(filters);
                tvPercent.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                tvPercent.setTextSize(16);
                tvPercent.setText(String.valueOf(percent));
                tvPercent.setGravity(Gravity.CENTER);

                rowLayout.addView(tvPercent);
                rowLayout.addView(colorView);

                parentView.addView(rowLayout);
            }
            Button btnGenerate = new Button(this);
            btnGenerate.setLayoutParams(buttonParams);
            btnGenerate.setText("GENERATE");
            btnGenerate.applyStyle(R.style.FlatWaveButtonRippleStyle);
            btnGenerate.setBackgroundColor(Color.WHITE);
            btnGenerate.setGravity(Gravity.CENTER);
            btnGenerate.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /*if (mInterstitialAd.isLoaded()) mInterstitialAd.show();
                            else openActivity();*/
                            openActivity();
                        }
                    }

            );
            parentView.addView(btnGenerate);
            parentView.invalidate();
        }
    }

    private void openActivity() {
        Intent intent = new Intent(CreatePaletteActivity.this, PaletteBackgroundActivity.class);
        intent.putExtra("numberOfColors", numberOfColor);
        intent.putExtra("isHorizontal", !radioHoz.isChecked());
        String colorPercent;
        int eachPercent = 100 / numberOfColor, i = 0, j = 0;
        int totalPercent = 0;
        do {
            View view = parentView.getChildAt(i);
            if (view instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) view;
                tvPercent = (EditText) layout.getChildAt(0);
                colorView = layout.getChildAt(1);
                int color = (int) colorView.getTag();

                if (!TextUtils.isEmpty(tvPercent.getText().toString())) {
                    eachPercent = Integer.parseInt(tvPercent.getText().toString());
                }
                totalPercent += eachPercent;
                colorPercent = eachPercent + Constant.VAR_TOKEN + color;
                intent.putExtra("colorPercent" + j, colorPercent);
                j++;
            }
            i++;
        }
        while (i <= parentView.getChildCount());
        intent.putExtra("totalPercent", totalPercent);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_palette, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                com.rey.material.app.Dialog.Builder builder = new SimpleDialog.Builder(R.style.SimpleDialogLight) {
                };

                ((SimpleDialog.Builder) builder).message(getString(R.string.help_create_palette))
                        .title("Help")
                        .positiveAction("CLOSE");
                DialogFragment fragment = DialogFragment.newInstance(builder);
                fragment.show(getSupportFragmentManager(), null);
                break;
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
