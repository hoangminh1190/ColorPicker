package com.m2team.colorpicker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.InterstitialAd;
import com.m2team.colorpicker.colorpallette.ItemDetailActivity;
import com.m2team.colorpicker.colorpallette.ItemDetailFragment;
import com.m2team.colorpicker.colorpallette.ItemListFragment;
import com.m2team.colorpicker.function.BookmarkActivityFragment;
import com.m2team.colorpicker.function.CompareColorFragment;
import com.m2team.colorpicker.function.ConvertColorFragment;
import com.m2team.colorpicker.function.CustomViewPager;
import com.m2team.colorpicker.function.MaterialColorFragment;
import com.m2team.colorpicker.function.RecentColorActivityFragment;
import com.m2team.colorpicker.utils.Utils;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.app.ToolbarManager;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.SnackBar;
import com.rey.material.widget.TabPageIndicator;
import com.rey.material.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainSettingActivity extends ActionBarActivity implements ItemListFragment.Callbacks, ToolbarManager.OnToolbarGroupChangedListener {

    private CustomViewPager vp;
    private PagerAdapter mPagerAdapter;
    private SnackBar mSnackBar;

    private DrawerAdapter mDrawerAdapter;
    private FrameLayout fl_drawer;
    private DrawerLayout dl_navigator;
    private ToolbarManager mToolbarManager;
    InterstitialAd mInterstitialAd;
    private final Tab[] mItems = new Tab[]{Tab.BOOKMARK, Tab.RECENT_COLOR, Tab.MATERIAL_COLOR, Tab.CONVERT, Tab.COMPARE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_setting);
        dl_navigator = (DrawerLayout) findViewById(R.id.main_dl);
        fl_drawer = (FrameLayout) findViewById(R.id.main_fl_drawer);
        ListView lv_drawer = (ListView) findViewById(R.id.main_lv_drawer);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        vp = (CustomViewPager) findViewById(R.id.main_vp);
        TabPageIndicator tpi = (TabPageIndicator) findViewById(R.id.main_tpi);
        mSnackBar = (SnackBar) findViewById(R.id.main_sn);

        mToolbarManager = new ToolbarManager(this, mToolbar, 0, R.style.ToolbarRippleStyle, R.anim.abc_fade_in, R.anim.abc_fade_out);
        mToolbarManager.setNavigationManager(new ToolbarManager.BaseNavigationManager(R.style.NavigationDrawerDrawable, this, mToolbar, dl_navigator) {
            @Override
            public void onNavigationClick() {
                if (mToolbarManager.getCurrentGroup() != 0)
                    mToolbarManager.setCurrentGroup(0);
                else
                    dl_navigator.openDrawer(fl_drawer);
            }

            @Override
            public boolean isBackState() {
                return super.isBackState() || mToolbarManager.getCurrentGroup() != 0;
            }

            @Override
            protected boolean shouldSyncDrawerSlidingProgress() {
                return super.shouldSyncDrawerSlidingProgress() && mToolbarManager.getCurrentGroup() == 0;
            }

        });
        mToolbarManager.registerOnToolbarGroupChangedListener(this);

        mDrawerAdapter = new DrawerAdapter();
        lv_drawer.setAdapter(mDrawerAdapter);

        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), mItems);
        vp.setAdapter(mPagerAdapter);
        tpi.setViewPager(vp);
        tpi.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (position == Tab.BOOKMARK.ordinal()) {
                    if (mPagerAdapter.mFragments[position] instanceof BookmarkActivityFragment) {
                        BookmarkActivityFragment fragment = (BookmarkActivityFragment) mPagerAdapter.mFragments[position];
                        if (fragment != null) fragment.onDataChangeListener();
                    }
                } else if (position == Tab.RECENT_COLOR.ordinal()) {
                    if (mPagerAdapter.mFragments[position] instanceof RecentColorActivityFragment) {
                        RecentColorActivityFragment fragment = (RecentColorActivityFragment) mPagerAdapter.mFragments[position];
                        if (fragment != null) fragment.onDataChangeListener();
                    }
                }
                mDrawerAdapter.setSelected(mItems[position]);
                mSnackBar.dismiss();
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

        });

        Intent intent = getIntent();
        if (intent != null) {
            int index = intent.getIntExtra("index", 0);
            vp.setCurrentItem(index);
            mDrawerAdapter.setSelected(mItems[index]);
        } else {
            vp.setCurrentItem(0);
            mDrawerAdapter.setSelected(Tab.BOOKMARK);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            int index = intent.getIntExtra("index", 0);
            vp.setCurrentItem(index);
            mDrawerAdapter.setSelected(mItems[index]);
        } else {
            vp.setCurrentItem(0);
            mDrawerAdapter.setSelected(Tab.BOOKMARK);
        }
    }

    @Override
    public void onItemSelected(int position) {
        Intent detailIntent = new Intent(this, ItemDetailActivity.class);
        detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, position);
        startActivity(detailIntent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mToolbarManager.onPrepareMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String title = "", msg = "";
        int currentItem = vp.getCurrentItem();
        if (currentItem == Tab.BOOKMARK.ordinal()) {
            title = Tab.BOOKMARK.name;
            msg  = getString(R.string.help_bookmark);
        } else if (currentItem == Tab.RECENT_COLOR.ordinal()) {
            title = Tab.RECENT_COLOR.name;
            msg  = getString(R.string.help_recent);
        } else if (currentItem == Tab.MATERIAL_COLOR.ordinal()) {
            title = Tab.MATERIAL_COLOR.name;
            msg  = getString(R.string.help_material_colors);
        } else if (currentItem == Tab.COMPARE.ordinal()) {
            title = Tab.COMPARE.name;
            msg  = getString(R.string.help_compare);
        } else if (currentItem == Tab.CONVERT.ordinal()) {
            title = Tab.CONVERT.name;
            msg  = getString(R.string.help_convert);
        }
        switch (item.getItemId()) {
            case R.id.action_share:
                String text = Utils.getFromClipboard(this);
                if (!TextUtils.isEmpty(text)) {
                    // Fetch and store ShareActionProvider
                    Intent mShareIntent = new Intent();
                    mShareIntent.setAction(Intent.ACTION_SEND);
                    mShareIntent.setType("text/plain");
                    mShareIntent.putExtra(Intent.EXTRA_TEXT, text);
                    startActivity(mShareIntent);
                } else {
                    Toast.makeText(this, "You must copy one color to share", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_help:
                Dialog.Builder builder = new SimpleDialog.Builder(R.style.SimpleDialogLight) {
                };

                ((SimpleDialog.Builder) builder).message(msg)
                        .title(title)
                        .positiveAction("CLOSE");
                DialogFragment fragment = DialogFragment.newInstance(builder);
                fragment.show(getSupportFragmentManager(), null);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onToolbarGroupChanged(int i, int i1) {
        mToolbarManager.notifyNavigationStateChanged();
    }

    public enum Tab {
        BOOKMARK("Bookmark"),
        RECENT_COLOR("Recent colors"),
        MATERIAL_COLOR("Material colors"),
        CONVERT("Convert"),
        COMPARE("Compare");
        private final String name;

        Tab(String s) {
            name = s;
        }

        public boolean equalsName(String otherName) {
            return (otherName != null) && name.equals(otherName);
        }

        public String toString() {
            return name;
        }

    }

    private static class PagerAdapter extends FragmentStatePagerAdapter {

        private static final Field sActiveField;

        static {
            Field f = null;
            try {
                Class<?> c = Class.forName("android.support.v4.app.FragmentManagerImpl");
                f = c.getDeclaredField("mActive");
                f.setAccessible(true);
            } catch (Exception e) {
            }

            sActiveField = f;
        }

        final Fragment[] mFragments;
        final Tab[] mTabs;

        public PagerAdapter(FragmentManager fm, Tab[] tabs) {
            super(fm);
            mTabs = tabs;
            mFragments = new Fragment[mTabs.length];


            //dirty way to get reference of cached fragment
            try {
                ArrayList<Fragment> mActive = (ArrayList<Fragment>) sActiveField.get(fm);
                if (mActive != null) {
                    for (Fragment fragment : mActive) {
                        if (fragment instanceof BookmarkActivityFragment)
                            setFragment(Tab.BOOKMARK, fragment);
                        if (fragment instanceof RecentColorActivityFragment)
                            setFragment(Tab.RECENT_COLOR, fragment);
                        else if (fragment instanceof MaterialColorFragment)
                            setFragment(Tab.MATERIAL_COLOR, fragment);
                        else if (fragment instanceof CompareColorFragment)
                            setFragment(Tab.COMPARE, fragment);
                        else if (fragment instanceof ConvertColorFragment)
                            setFragment(Tab.CONVERT, fragment);
                    }
                }
            } catch (Exception e) {
            }
        }

        private void setFragment(Tab tab, Fragment f) {
            for (int i = 0; i < mTabs.length; i++)
                if (mTabs[i] == tab) {
                    mFragments[i] = f;
                    break;
                }
        }

        @Override
        public Fragment getItem(int position) {
            if (mFragments[position] == null) {
                switch (mTabs[position]) {

                    case BOOKMARK:
                        mFragments[position] = BookmarkActivityFragment.newInstance();
                        break;
                    case RECENT_COLOR:
                        mFragments[position] = RecentColorActivityFragment.newInstance();
                        break;
                    case MATERIAL_COLOR:
                        mFragments[position] = MaterialColorFragment.newInstance();
                        break;
                    case CONVERT:
                        mFragments[position] = ConvertColorFragment.newInstance();
                        break;
                    case COMPARE:
                        mFragments[position] = CompareColorFragment.newInstance();
                        break;
                }
            }

            return mFragments[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs[position].toString().toUpperCase();
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }
    }

    class DrawerAdapter extends BaseAdapter implements View.OnClickListener {

        private Tab mSelectedTab;

        public void setSelected(Tab tab) {
            if (tab != mSelectedTab) {
                mSelectedTab = tab;
                notifyDataSetInvalidated();
            }
        }

        public Tab getSelectedTab() {
            return mSelectedTab;
        }

        @Override
        public int getCount() {
            return mItems.length;
        }

        @Override
        public Object getItem(int position) {
            return mItems[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(MainSettingActivity.this).inflate(R.layout.row_drawer, null);
                v.setOnClickListener(this);
            }

            v.setTag(position);
            Tab tab = (Tab) getItem(position);
            ((TextView) v).setText(tab.toString());

            if (tab == mSelectedTab) {
                v.setBackgroundColor(ThemeUtil.colorPrimary(MainSettingActivity.this, 0));
                ((TextView) v).setTextColor(0xFFFFFFFF);
            } else {
                v.setBackgroundResource(0);
                ((TextView) v).setTextColor(0xFF000000);
            }

            return v;
        }

        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();
            vp.setCurrentItem(position);
            dl_navigator.closeDrawer(fl_drawer);
        }
    }
}

