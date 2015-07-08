package com.m2team.colorpicker.colorpallette;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.m2team.colorpicker.R;
import com.m2team.colorpicker.colorpallette.color.ColorPalette;

import java.util.List;

public class ItemListAdapter extends BindableAdapter<ColorPalette.ColorGroup> {

    public ItemListAdapter(Context context){
        super(context, ColorPalette.ITEMS);
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container){
        View view = inflater.inflate(R.layout.list_item_list_color, container, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(ColorPalette.ColorGroup item, int position, View view){
        ViewHolder holder = (ViewHolder)view.getTag();
        holder.item.setBackgroundColor(getContext().getResources().getColor(item.getColor()));
        holder.name.setText(item.getColorName());
        holder.name.setTextColor(getContext().getResources().getColor(item.getTextColor()));
        holder.name.setBackgroundColor(getContext().getResources().getColor(item.getColor()));
    }

    private class ViewHolder {

        final RelativeLayout item;
        final TextView name;

        public ViewHolder(View view){
            item = (RelativeLayout)view.findViewById(R.id.item);
            name = (TextView)view.findViewById(R.id.name);
        }
    }
}
