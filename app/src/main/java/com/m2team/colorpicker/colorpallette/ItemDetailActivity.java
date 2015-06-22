package com.m2team.colorpicker.colorpallette;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;

import com.m2team.colorpicker.R;
import com.m2team.colorpicker.colorpallette.color.ColorPalette;

import java.util.Locale;

public class ItemDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_item_detail);

        ActionBar supportActionBar = getSupportActionBar();
        if(supportActionBar != null){
            supportActionBar.setDisplayShowHomeEnabled(false);
            supportActionBar.setDisplayUseLogoEnabled(false);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        if(savedInstanceState == null){
            int position = getIntent().getIntExtra(ItemDetailFragment.ARG_ITEM_ID, 0);
            ColorPalette.ColorGroup color = ColorPalette.ITEMS.get(position);
            String titleText = getResources().getString(color.getColorName());
            int textColor = getResources().getColor(color.getTextColor());
            String htmlColor = String.format(Locale.US, "#%06X", (0xFFFFFF & Color.argb(0, Color.red(textColor), Color.green(textColor), Color.blue(textColor))));
            String titleHtml = "<font color=\"" + htmlColor + "\">" + titleText + "</font>";
            if(supportActionBar != null){
                supportActionBar.setTitle(Html.fromHtml(titleHtml));
                supportActionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(color.getColor())));
            }
            Bundle arguments = new Bundle();
            arguments.putInt(ItemDetailFragment.ARG_ITEM_ID, position);
            final ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().add(R.id.item_detail_container, fragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == android.R.id.home){
            super.onBackPressed();
            //NavUtils.navigateUpTo(this, new Intent(this, MainSettingActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
