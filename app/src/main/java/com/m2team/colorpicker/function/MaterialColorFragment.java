package com.m2team.colorpicker.function;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.m2team.colorpicker.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class MaterialColorFragment extends Fragment {
    public MaterialColorFragment() {
    }

    public static MaterialColorFragment newInstance() {
        return new MaterialColorFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }


}
