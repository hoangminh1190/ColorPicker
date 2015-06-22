package com.m2team.colorpicker.colorpallette;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.m2team.colorpicker.R;
import com.m2team.colorpicker.colorpallette.color.MaterialDesignColor;

import java.util.List;

public class ItemDetailAdapter extends BindableAdapter<MaterialDesignColor> {

    public ItemDetailAdapter(Context context, List<MaterialDesignColor> items){
        super(context, items);
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container){
        View view = inflater.inflate(R.layout.list_item_detail_color, container, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(MaterialDesignColor item, int position, View view){
        ViewHolder holder = (ViewHolder)view.getTag();
        holder.item.setBackgroundColor(getContext().getResources().getColor(item.getColor()));
        holder.name.setText(item.getColorName());
        holder.name.setTextColor(getContext().getResources().getColor(item.getTextColor()));
        holder.code.setText(item.getColorCode());
        holder.code.setTextColor(getContext().getResources().getColor(item.getTextColor()));
    }

    private class ViewHolder {

        RelativeLayout item;
        TextView name;
        TextView code;

        public ViewHolder(View view){
            item = (RelativeLayout)view.findViewById(R.id.item);
            name = (TextView)view.findViewById(R.id.name);
            code = (TextView)view.findViewById(R.id.code);
        }
    }
}
