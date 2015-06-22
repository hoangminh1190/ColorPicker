package com.aidangrabe.materialcolorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.Preference;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.aidangrabe.materialcolorpicker.views.ColorCircleView;

/**
 * Created by aidan on 30/04/15.
 * A Preference item for choosing a colour
 */
public class ColorPickerPreference extends Preference
        implements Preference.OnPreferenceClickListener, ColorPickerDialogFragment.ColorSelectedListener {

    private FragmentManager mFragmentManager;
    private ColorCircleView mCircleView;
    private int mColor;

    public ColorPickerPreference(Context context) {
        this(context, null, 0);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnPreferenceClickListener(this);
        setWidgetLayoutResource(R.layout.color_preference);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Log.d("D", "Preference clicked");
        showColorPicker();
        return false;
    }

    private void showColorPicker() {

        if (mFragmentManager == null) {
            throw new IllegalStateException("FragmentManager must be set for this Preference. eg. findPreference('pref_name').setFragmentManager");
        }

        ColorPickerDialogFragment colorPickerDialogFragment = new ColorPickerDialogFragment();
        colorPickerDialogFragment.setOnColorSelectedListener(this);
        colorPickerDialogFragment.show(mFragmentManager, "tag");

    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String hexColor = a.getString(index);
        Log.d("D", "HEX: " + hexColor);
        return Color.parseColor(hexColor);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

        Integer color = defaultValue == null ? 0 : (int) defaultValue;
        if (restorePersistedValue) {
            color = getPersistedInt(0);
        }

        mColor = color;

    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mCircleView = (ColorCircleView) view.findViewById(R.id.color_view);
        mCircleView.setColor(mColor);

    }

    @Override
    public void onColorSelected(int color) {

        OnPreferenceChangeListener preferenceChangeListener = getOnPreferenceChangeListener();
        if (preferenceChangeListener != null) {
            preferenceChangeListener.onPreferenceChange(this, color);
        }

        mColor = color;
        mCircleView.setColor(color);

        persistInt(color);

    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

}
