package com.m2team.colorpicker.function;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.m2team.colorpicker.MainSettingActivity;
import com.m2team.colorpicker.R;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.Button;

import java.util.ArrayList;

public class LongPressDialogFragment extends DialogFragment implements View.OnClickListener {

    private static Context context;
    private String value;
    private String fragmentId;
    private IOnDataChangeListener listener;

    public LongPressDialogFragment() {
    }

    public static LongPressDialogFragment newInstance(Context c) {
        LongPressDialogFragment fragment = new LongPressDialogFragment();
        context = c;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            value = bundle.getString("value");
            fragmentId = bundle.getString("fragmentId");
        }
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().getAttributes().windowAnimations = R.style.SimpleDialogLight;
        }
        //setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (context == null) context = getActivity();
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.layout_dialog_longpress, container, false);
        init(view);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Utils.setColorDialog(dialog, value, Color.parseColor(value));
        return dialog;
    }

    private void onDismiss() {
        if (listener != null) listener.onDataChangeListener();
    }

    public void setLongPressListener(IOnDataChangeListener longPressListener) {
        listener = longPressListener;
    }

    private void init(View view) {
        Button tvCopy = (Button) view.findViewById(R.id.tv_copy);
        Button tvShare = (Button) view.findViewById(R.id.tv_share);
        Button tvDelete = (Button) view.findViewById(R.id.tv_delete);
        Button tvDeleteAll = (Button) view.findViewById(R.id.tv_delete_all);
        Button tv_bookmark = (Button) view.findViewById(R.id.tv_bookmark);
        Button tv_compare = (Button) view.findViewById(R.id.tv_compare);
        tvDelete.setOnClickListener(this);
        tvDeleteAll.setOnClickListener(this);
        tvShare.setOnClickListener(this);
        tvCopy.setOnClickListener(this);
        tv_bookmark.setOnClickListener(this);
        tv_compare.setOnClickListener(this);
        String firstColor = Utils.getPrefString(context, Constant.FIRST_COLOR_COMPARE);
        if (TextUtils.isEmpty(firstColor)) {
            tv_compare.setText("Choose to compare");
        } else {
            tv_compare.setText("Compare with " + firstColor);
        }
        switch (fragmentId) {
            case "main":
                tvDelete.setVisibility(View.GONE);
                tvDeleteAll.setVisibility(View.GONE);
                tv_bookmark.setVisibility(View.VISIBLE);
                break;
            case "bookmark":
                tvDelete.setVisibility(View.VISIBLE);
                tvDeleteAll.setVisibility(View.VISIBLE);
                tv_bookmark.setVisibility(View.GONE);
                break;
            case "recent":
                tvDelete.setVisibility(View.VISIBLE);
                tvDeleteAll.setVisibility(View.VISIBLE);
                tv_bookmark.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_copy:
                if (!TextUtils.isEmpty(value)) {
                    Utils.copyToClipboard(context, value);
                    Utils.putSharedPrefStringSetValue(context, Constant.COLOR_RECENT_LIST, value);
                    Toast.makeText(context, "Copied " + value + " to clipboard", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_share:
                // Fetch and store ShareActionProvider
                Intent mShareIntent = new Intent();
                mShareIntent.setAction(Intent.ACTION_SEND);
                mShareIntent.setType("text/plain");
                mShareIntent.putExtra(Intent.EXTRA_TEXT, value);
                startActivity(mShareIntent);
                break;
            case R.id.tv_bookmark:
                Utils.putSharedPrefStringSetValue(context, Constant.COLOR_BOOKMARK_LIST, value);
                Toast.makeText(context, "Added " + value + " to bookmark", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_compare:
                String firstColor = Utils.getPrefString(context, Constant.FIRST_COLOR_COMPARE);
                String secondColor = Utils.getPrefString(context, Constant.SECOND_COLOR_COMPARE);
                if (TextUtils.isEmpty(firstColor)) {
                    Utils.putPrefValue(context, Constant.FIRST_COLOR_COMPARE, value);
                    Toast.makeText(context, value + " chosen. Choose another color to compare", Toast.LENGTH_SHORT).show();
                    break;
                } else if (TextUtils.isEmpty(secondColor)) {
                    Utils.putPrefValue(context, Constant.SECOND_COLOR_COMPARE, value);
                }
                Intent intent = new Intent(context, MainSettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("index", MainSettingActivity.Tab.COMPARE.ordinal());
                startActivity(intent);
                break;
            case R.id.tv_delete:
                delete();
                break;

            case R.id.tv_delete_all:
                SimpleDialog.Builder builder = new SimpleDialog.Builder(R.style.SimpleDialogLight) {
                    @Override
                    public void onPositiveActionClicked(com.rey.material.app.DialogFragment fragment) {
                        deleteAll();
                        super.onPositiveActionClicked(fragment);
                    }

                };

                builder.title("Do you want to clear all your data?")
                        .positiveAction("OK")
                        .negativeAction("CANCEL");
                com.rey.material.app.DialogFragment fragment = com.rey.material.app.DialogFragment.newInstance(builder);
                fragment.show(getFragmentManager(), "deleteAllDialog");
                break;
        }
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void delete() {
        ArrayList<String> list = null;
        int index = fragmentId.equals("bookmark") ? 0 : fragmentId.equals("recent") ? 1 : 2;
        if (index == 0) {
            list = Utils.getSharedPrefStringSetValue(context, Constant.COLOR_BOOKMARK_LIST);
        } else if (index == 1) {
            list = Utils.getSharedPrefStringSetValue(context, Constant.COLOR_RECENT_LIST);
        }
        if (list != null && list.size() > 0) {
            boolean remove = list.remove(value);
            if (remove) {
                Toast.makeText(context, "Delete success", Toast.LENGTH_SHORT).show();
                Utils.saveToJson(getActivity(), index == 0 ? Constant.COLOR_BOOKMARK_LIST : Constant.COLOR_RECENT_LIST, list);
                onDismiss();
            } else {
                Toast.makeText(context, "Delete fail. Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Cannot delete color", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAll() {
        boolean remove = false;
        int index = fragmentId.equals("bookmark") ? 0 : fragmentId.equals("recent") ? 1 : 2;
        if (index == 0) {
            Utils.clearStringSet(context, Constant.COLOR_BOOKMARK_LIST);
            ArrayList<String> set = Utils.getSharedPrefStringSetValue(context, Constant.COLOR_BOOKMARK_LIST);
            remove = set == null || set.size() == 0;
        } else if (index == 1) {
            Utils.clearStringSet(context, Constant.COLOR_RECENT_LIST);
            ArrayList<String> set = Utils.getSharedPrefStringSetValue(context, Constant.COLOR_RECENT_LIST);
            remove = set == null || set.size() == 0;
        }
        if (remove) {
            Toast.makeText(context, "Clear success", Toast.LENGTH_SHORT).show();
            onDismiss();
        } else {
            Toast.makeText(context, "Clear fail. Please try again", Toast.LENGTH_SHORT).show();
        }
    }

}
