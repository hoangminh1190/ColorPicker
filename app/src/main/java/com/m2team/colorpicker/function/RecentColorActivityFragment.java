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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.m2team.colorpicker.R;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class RecentColorActivityFragment extends Fragment implements IOnDataChangeListener {
    private ListView listView;
    private ListAdapter adapter;
    private ArrayList<String> recentColors;
    private static Context context;

    public RecentColorActivityFragment() {
    }

    public static RecentColorActivityFragment newInstance() {
        return new RecentColorActivityFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (context == null) context = getActivity();
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        AdView mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest.Builder builder = new AdRequest.Builder();
        builder.addTestDevice(getString(R.string.test_device_id_n910f));
        builder.addTestDevice(getString(R.string.test_device_id_htc));
        AdRequest adRequest = builder.build();
        mAdView.loadAd(adRequest);
        listView = (ListView) view.findViewById(R.id.list_bookmark_color);

        recentColors = new ArrayList<>(Utils.getSharedPrefStringSetValue(context, Constant.COLOR_RECENT_LIST));
        adapter = new ListAdapter(context, recentColors);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
            recentColors = Utils.getSharedPrefStringSetValue(context, Constant.COLOR_RECENT_LIST);
            adapter = new ListAdapter(context, recentColors);
            listView.setAdapter(adapter);
        }
    }
}
