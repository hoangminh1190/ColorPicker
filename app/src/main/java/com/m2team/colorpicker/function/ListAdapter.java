package com.m2team.colorpicker.function;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.m2team.colorpicker.utils.Constant;
import com.m2team.colorpicker.R;
import com.m2team.colorpicker.utils.Utils;
import com.rey.material.widget.SnackBar;


public class ListAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> lstItem = new ArrayList<>();
    SnackBar snackBar;

    public ListAdapter(Context c, ArrayList<String> lstItem, SnackBar snackbar) {
        context = c;
        this.lstItem = lstItem;
        this.snackBar = snackbar;
    }

    @Override
    public int getCount() {
        return lstItem.size();
    }

    @Override
    public String getItem(int position) {
        return lstItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null) {// if current item is not showing --> must re-init it
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.layout_list_item_bookmark, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) view
                    .findViewById(R.id.book_mark_each_info);
            holder.color = view.findViewById(R.id.bookmark_each_color);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        String info = getItem(position);
        if (!TextUtils.isEmpty(info)) {
            holder.name.setText(info);
            holder.color.setBackgroundColor(Color.parseColor(info));
            view.setTag(holder);
        }
        return view;
    }

    private static class ViewHolder {
        private TextView name;
        private View color;
    }
}
