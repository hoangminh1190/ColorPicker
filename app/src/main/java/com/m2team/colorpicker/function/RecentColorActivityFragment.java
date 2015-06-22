package com.m2team.colorpicker.function;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.m2team.colorpicker.R;
import com.m2team.colorpicker.utils.Applog;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;
import com.rey.material.widget.SnackBar;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class RecentColorActivityFragment extends Fragment implements IOnDataChangeListener {
    SnackBar snackbar;
    ListView listView;
    ListAdapter adapter;
    ArrayList<String> recentColors;
    static Context context;

    public RecentColorActivityFragment() {
    }

    public static RecentColorActivityFragment newInstance() {
        RecentColorActivityFragment fragment = new RecentColorActivityFragment();
        Applog.e("newInstance");
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (context == null) context = getActivity();
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        snackbar = (SnackBar) view.findViewById(R.id.snackbar);
        listView = (ListView) view.findViewById(R.id.list_bookmark_color);
        recentColors = new ArrayList<>(Utils.getSharedPrefStringSetValue(context, Constant.COLOR_RECENT_LIST));
        adapter = new ListAdapter(context, recentColors, snackbar);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Applog.e("onItemClick");
                ColorDetailDialogFragment colorDetailDialogFragment = new ColorDetailDialogFragment();
                Bundle bundle = new Bundle();
                String hex = recentColors.get(position);
                String fullInfoColor = Utils.setFullInfoColor(Color.parseColor(hex));
                bundle.putString("detailColor", fullInfoColor);
                colorDetailDialogFragment.setArguments(bundle);
                colorDetailDialogFragment.show(getFragmentManager(), "detailDialog");
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Applog.e("onItemLongClick");
                LongPressDialogFragment fragment = LongPressDialogFragment.newInstance(context);
                Bundle bundle = new Bundle();
                bundle.putString("value", recentColors.get(position));
                bundle.putString("fragmentId", "recent");
                fragment.setArguments(bundle);
                fragment.show(getFragmentManager(), "longPressDialog");
                fragment.setLongPressListener(RecentColorActivityFragment.this);
                return true;
            }
        });
        return view;
    }

    @Override
    public void onDataChangeListener() {
        if (context != null && listView != null) {
            recentColors = new ArrayList<>(Utils.getSharedPrefStringSetValue(context, Constant.COLOR_RECENT_LIST));
            adapter = new ListAdapter(context, recentColors, snackbar);
            listView.setAdapter(adapter);
        }
    }
}
