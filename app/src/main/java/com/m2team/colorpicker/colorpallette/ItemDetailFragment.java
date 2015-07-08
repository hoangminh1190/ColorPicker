package com.m2team.colorpicker.colorpallette;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.m2team.colorpicker.colorpallette.color.ColorPalette;
import com.m2team.colorpicker.colorpallette.color.MaterialDesignColor;
import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.utils.Utils;

import java.util.List;

public class ItemDetailFragment extends ListFragment {

    public static final String ARG_ITEM_ID = "item_id";

    public ItemDetailFragment(){
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        if(getArguments().containsKey(ARG_ITEM_ID)){
            ColorPalette.ColorGroup color = ColorPalette.ITEMS.get(getArguments().getInt(ARG_ITEM_ID));
            List<MaterialDesignColor> items = ColorPalette.ITEM_MAP.get(color);
            ItemDetailAdapter adapter = new ItemDetailAdapter(getActivity(), items);
            setListAdapter(adapter);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        getListView().setDivider(null);
        getListView().setDividerHeight(0);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id){
        super.onListItemClick(listView, view, position, id);

        MaterialDesignColor color = (MaterialDesignColor)listView.getItemAtPosition(position);
        String colorCode = getString(color.getColorCode());
        Utils.putSharedPrefStringSetValue(getActivity(), Constant.COLOR_RECENT_LIST, colorCode);
        copyText(getActivity(), colorCode);
        Toast.makeText(getActivity(), "Copied " + colorCode + " to clipboard", Toast.LENGTH_SHORT).show();
    }


    @SuppressWarnings("deprecation")
    private void copyText(Context context, String text){
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB){
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager)context.getSystemService(
                Context.CLIPBOARD_SERVICE
            );
            clipboard.setText(text);
        }else{
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)context.getSystemService(
                Context.CLIPBOARD_SERVICE
            );
            android.content.ClipData clip = android.content.ClipData.newPlainText(
                "Copied color code", text
            );
            clipboard.setPrimaryClip(clip);
        }
    }

}
