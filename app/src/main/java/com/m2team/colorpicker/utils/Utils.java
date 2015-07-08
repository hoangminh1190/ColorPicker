package com.m2team.colorpicker.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rey.material.widget.SnackBar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hoang Minh on 6/15/2015.
 */
public class Utils {
    public static final String ROBOTO_REGULAR = "RobotoCondensed-Regular.ttf";
    public static final String ROBOTO_LIGHT = "RobotoCondensed-Light.ttf";
    public static final String ROBOTO_BOLD = "RobotoCondensed-Bold.ttf";
    public static final String ROBOTO_ITALIC = "RobotoCondensed-Italic.ttf";
    private static final String FONT_PATH = "fonts/";

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {
        final String column = "_data";
        final String[] projection = {
                column
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            final int column_index = cursor.getColumnIndexOrThrow(column);
            String s = cursor.getString(column_index);
            cursor.close();
            return s;
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getPrefString(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.PREF_FILE_NAME, Context.MODE_MULTI_PROCESS);
        return sharedPreferences.getString(key, "");
    }

    public static int getPrefInt(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.PREF_FILE_NAME, Context.MODE_MULTI_PROCESS);
        return sharedPreferences.getInt(key, 0);
    }

    public static boolean getPrefBoolean(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.PREF_FILE_NAME, Context.MODE_MULTI_PROCESS);
        return sharedPreferences.getBoolean(key, false);
    }

    public static void putPrefValue(Context context, String key, Object value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value instanceof Integer)
            editor.putInt(key, Integer.parseInt(value.toString()));
        else if (value instanceof String)
            editor.putString(key, value.toString());
        else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        }
        editor.apply();
    }

    public static void clearStringSet(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(Constant.PREF_FILE_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key);
        editor.apply();
    }

    public static float dp2px(Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    public static File createImageFile(String fileName) throws IOException {
        // Create an image file name
        File dir = new File(Environment.getExternalStorageDirectory() + "/" + Constant.FOLDER_NAME + "/");
        if (!dir.exists()) {
            boolean mkdir = dir.mkdir();
            if (!mkdir) return null;
        }
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            boolean result;
            File f = new File(dir, fileName + ".jpg");
            result =  f.createNewFile();
            if (result) return f;
            else return File.createTempFile(fileName, ".jpg", dir);
        }
        return null;
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData primaryClip = ClipData.newPlainText("", text);
        clipboardManager.setPrimaryClip(primaryClip);

    }

    public static String getFromClipboard(Context context) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager.hasPrimaryClip() && clipboardManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            ClipData.Item itemAt = clipboardManager.getPrimaryClip().getItemAt(0);
            if (itemAt != null) {
                return itemAt.getText().toString();
            }
        }
        return null;
    }

    public static void showMessage(SnackBar snackBar, String text) {
        if (snackBar != null)
            snackBar
                    .text(text)
                    .duration(2000).actionText("")
                    .show();
    }

    private static String getColorHex(String colorInfo) {
        String[] split = colorInfo.split(Constant.VAR_TOKEN);
        if (split.length > 0)
            return split[0];
        return "";
    }

    private static String getColorRgb(String colorInfo) {
        String[] split = colorInfo.split(Constant.VAR_TOKEN);
        if (split.length > 1)
            return split[1];
        return "";
    }

    private static String getColorHsv(String colorInfo) {
        String[] split = colorInfo.split(Constant.VAR_TOKEN);
        if (split.length > 2)
            return split[2];
        return "";
    }

    private static String getColorHsl(String colorInfo) {
        String[] split = colorInfo.split(Constant.VAR_TOKEN);
        if (split.length > 3)
            return split[3];
        return "";
    }

    private static String getColorCMYK(String colorInfo) {
        String[] split = colorInfo.split(Constant.VAR_TOKEN);
        if (split.length > 4)
            return split[4];
        return "";
    }

    private static String getColorLab(String colorInfo) {
        String[] split = colorInfo.split(Constant.VAR_TOKEN);
        if (split.length > 5)
            return split[5];
        return "";
    }

    private static String getColorXYZ(String colorInfo) {
        String[] split = colorInfo.split(Constant.VAR_TOKEN);
        if (split.length > 6)
            return split[6];
        return "";
    }

    private static String getFullInfoColor(String hexColor, String rgb, String hsv, String hsl, String cmyk, String lab, String xyz) {
        return hexColor + Constant.VAR_TOKEN + rgb + Constant.VAR_TOKEN + hsv + Constant.VAR_TOKEN
                + hsl + Constant.VAR_TOKEN + cmyk + Constant.VAR_TOKEN
                + lab + Constant.VAR_TOKEN + xyz;
    }

    public static String setFullInfoColor(int pixel) {
        ColorSpaceConverter converter = new ColorSpaceConverter();
        float[] hsv = new float[3];
        String hex = converter.rgbToHex(Color.red(pixel), Color.green(pixel), Color.blue(pixel));
        Color.colorToHSV(pixel, hsv);
        float[] cmyk = converter.rgbToCmyk(new float[]{Color.red(pixel), Color.green(pixel), Color.blue(pixel)});
        float[] hsl = converter.rgbToHSL(new float[]{Color.red(pixel), Color.green(pixel), Color.blue(pixel)});
        double[] lab = converter.RGBtoLAB(new int[]{Color.red(pixel), Color.green(pixel), Color.blue(pixel)});
        double[] xyz = converter.RGBtoXYZ(new int[]{Color.red(pixel), Color.green(pixel), Color.blue(pixel)});

        String srgb = Color.red(pixel) + Constant.DOLLAR_TOKEN + Color.green(pixel) + Constant.DOLLAR_TOKEN + Color.blue(pixel);
        String shsv = Utils.round(hsv[0], 0) + Constant.DOLLAR_TOKEN + Utils.round(hsv[1], 2) * 100 + Constant.DOLLAR_TOKEN + Utils.round(hsv[2], 2) * 100;
        String shsl = hsl[0] + Constant.DOLLAR_TOKEN + hsl[1] + Constant.DOLLAR_TOKEN + hsl[2];
        String scmyk = cmyk[0] + Constant.DOLLAR_TOKEN + cmyk[1] + Constant.DOLLAR_TOKEN + cmyk[2] + Constant.DOLLAR_TOKEN + cmyk[3];
        String slab = lab[0] + Constant.DOLLAR_TOKEN + lab[1] + Constant.DOLLAR_TOKEN + lab[2];
        String sxyz = xyz[0] + Constant.DOLLAR_TOKEN + xyz[1] + Constant.DOLLAR_TOKEN + xyz[2];
        return getFullInfoColor(hex, srgb, shsv, shsl, scmyk, slab, sxyz);
    }

    public static String getOneColorMode(String fullInfoColor, int index) {
        switch (index) {
            case 0:
                return getColorHex(fullInfoColor);

            case 1:
                return getColorRgb(fullInfoColor);

            case 2:
                return getColorHsv(fullInfoColor);

            case 3:
                return getColorHsl(fullInfoColor);

            case 4:
                return getColorCMYK(fullInfoColor);

            case 5:
                return getColorLab(fullInfoColor);

            case 6:
                return getColorXYZ(fullInfoColor);
        }
        return "";
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);

        value = value * factor;
        long tmp = Math.round(value);
        return (float) tmp / factor;
    }

    private static String roundZero(String s) {
        if (s.contains("."))
            return s.substring(0, s.indexOf("."));
        return s;
    }

    private static String roundZero(float value) {
        String s = String.valueOf(value);
        if (s.contains("."))
            return s.substring(0, s.indexOf("."));
        return s;
    }

    public static String setStyleHSV_HSL(float[] hsl) {
        return Utils.roundZero(hsl[0]) + Constant.degree + Constant.SPACE_TOKEN
                + Utils.roundZero(Utils.round(hsl[1], 2)) + "%"
                + Constant.SPACE_TOKEN
                + Utils.roundZero(Utils.round(hsl[2], 2)) + "%";
    }

    public static String setStyleHSV_HSL(String[] hsl) {
        return Utils.roundZero(hsl[0]) + Constant.degree + Constant.SPACE_TOKEN
                + Utils.roundZero(Utils.round(Float.parseFloat(hsl[1]), 2)) + "%"
                + Constant.SPACE_TOKEN
                + Utils.roundZero(Utils.round(Float.parseFloat(hsl[2]), 2)) + "%";
    }

    public static String setStyleCMYK(float[] cmyk) {
        return Utils.roundZero(cmyk[0]) + "%" + Constant.SPACE_TOKEN
                + Utils.roundZero(cmyk[1]) + "%" + Constant.SPACE_TOKEN
                + Utils.roundZero(cmyk[2]) + "%" + Constant.SPACE_TOKEN
                + Utils.roundZero(cmyk[3]) + "%";
    }

    public static String setStyleCMYK(String[] cmyk) {
        return Utils.roundZero(cmyk[0]) + "%" + Constant.SPACE_TOKEN
                + Utils.roundZero(cmyk[1]) + "%" + Constant.SPACE_TOKEN
                + Utils.roundZero(cmyk[2]) + "%" + Constant.SPACE_TOKEN
                + Utils.roundZero(cmyk[3]) + "%";
    }

    public static String setStyleLab_XYZ(String[] lab) {
        return lab[0] + Constant.SPACE_TOKEN + lab[1] + Constant.SPACE_TOKEN + lab[2];
    }

    public static String setStyleLab_XYZ(double[] lab) {
        return lab[0] + Constant.SPACE_TOKEN + lab[1] + Constant.SPACE_TOKEN + lab[2];
    }

    public static String setStyleRGB(String[] rgb) {
        return rgb[0] + Constant.SPACE_TOKEN + rgb[1] + Constant.SPACE_TOKEN + rgb[2];
    }

    public static String setStyleRGB(int[] rgb) {
        return rgb[0] + Constant.SPACE_TOKEN + rgb[1] + Constant.SPACE_TOKEN + rgb[2];
    }

    private static void setFont(final Context context, final View v, String font) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    setFont(context, child, font);
                }
            } else if (v instanceof TextView) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), FONT_PATH + font));
            }
        } catch (Exception e) {
        }
    }

    public static void setColorDialog(Dialog dialog, String title, int colorId) {
        SpannableString str = new SpannableString(title);
        str.setSpan(new ForegroundColorSpan(colorId), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        dialog.setTitle(str);
        Resources resources = dialog.getContext().getResources();
        int titleDividerId = resources.getIdentifier("titleDivider", "id", "android");
        View titleDivider = dialog.getWindow().getDecorView().findViewById(titleDividerId);
        if (titleDivider != null) {
            //titleDivider.setBackgroundColor(colorId);
            titleDivider.setVisibility(View.GONE);
        }
    }

    public static void setColorAlertDialogTitle(AlertDialog dialog, int color) {
        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        if (dividerId != 0) {
            View divider = dialog.findViewById(dividerId);
            //divider.setBackgroundColor(color);
            divider.setVisibility(View.GONE);
        }

        int textViewId = dialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
        if (textViewId != 0) {
            TextView tv = (TextView) dialog.findViewById(textViewId);
            tv.setTextColor(color);
        }

        int iconId = dialog.getContext().getResources().getIdentifier("android:id/icon", null, null);
        if (iconId != 0) {
            ImageView icon = (ImageView) dialog.findViewById(iconId);
            icon.setColorFilter(color);
        }
    }

    // used for store arrayList in json format
    public static void putSharedPrefStringSetValue(Context context, String key, String value) {
        ArrayList<String> values = getSharedPrefStringSetValue(context, key);
        if (values.contains(value.toLowerCase()) || values.contains(value.toUpperCase()))
            values.remove(value);
        values.add(0, value);
        saveToJson(context, key, values);
    }

    public static void saveToJson(Context context, String key, ArrayList<String> values) {
        SharedPreferences settings = context.getSharedPreferences(Constant.PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(values);
        editor.putString(key, jsonFavorites);
        editor.apply();
    }

    public static ArrayList<String> getSharedPrefStringSetValue(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(Constant.PREF_FILE_NAME, Context.MODE_PRIVATE);
        String jsonFavorites = settings.getString(key, "");
        if (TextUtils.isEmpty(jsonFavorites)) return new ArrayList<>();
        Gson gson = new Gson();
        return gson.fromJson(jsonFavorites, ArrayList.class);
    }

}
