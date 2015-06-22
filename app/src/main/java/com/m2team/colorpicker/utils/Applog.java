package com.m2team.colorpicker.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.MissingFormatArgumentException;
import java.util.UnknownFormatConversionException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Logging helper class. Refer to VolleyLog
 */
public class Applog {

    public static String TAG = "cpk";

    //public static boolean DEBUG = Log.isLoggable(TAG, Log.VERBOSE);
    public static boolean DEBUG = false;
    private static Applog sInstance;
    private SimpleDateFormat mFormatter;
    private Date mDate;
    private Logger mLogger;
    private boolean mFileLogEnabled = false;

    private Applog() {
        if (android.os.Build.TYPE.equals("eng")) {
            DEBUG = true;
            Log.d(TAG, "eng mode, debug log is enabled.");
        }

        mFormatter = new SimpleDateFormat("MM/dd HH:mm:ss.SSS", Locale.getDefault());
        mDate = new Date();

        mLogger = Logger.getLogger(TAG);
        if (mLogger != null) {
            mLogger.setLevel(Level.ALL);
            mLogger.setUseParentHandlers(false);
        } else {
            Log.e(TAG, "logger is null.");
        }
    }

    public static void initialize(Context context, String tag) {
        if (!TextUtils.isEmpty(tag)) {
            TAG = tag;
        }

        Applog log = getInstance();

        try {
            context.getAssets().open("filelog_enable").close();
            log.mFileLogEnabled = true;

        } catch (IOException e) {
            Log.e(TAG, e.toString());
            File file = new File(Environment.getExternalStorageDirectory() + "/filelog_enable");
            log.mFileLogEnabled = file.exists();
        }

        if (log.mFileLogEnabled && log.mLogger != null) {
            try {
                final int LOG_FILE_LIMIT = 1024 * 1024;
                final int LOG_FILE_MAX_COUNT = 4;

                StringBuilder logFilePath = new StringBuilder();
                logFilePath.append(Environment.getDataDirectory());
                logFilePath.append(File.separator);
                logFilePath.append("/data/com.samsung.android.app.sreminder/log");

                File directory = new File(logFilePath.toString());
                boolean result = directory.mkdir();
                Log.d(TAG, String.format("mkdir:%s", Boolean.toString(result)));

                logFilePath.append(File.separator);
                logFilePath.append(tag);
                logFilePath.append("%g%u.log");

                FileHandler fileHandler = new FileHandler(logFilePath.toString(), LOG_FILE_LIMIT, LOG_FILE_MAX_COUNT, true);
                fileHandler.setFormatter(new Formatter() {

                    @Override
                    public String format(LogRecord r) {
                        return r.getMessage() + "\n";
                    }
                });

                log.mLogger.addHandler(fileHandler);

            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }

        Log.d(TAG, String.format("fileLog:%s", (log.mFileLogEnabled ? "enabled" : "disabled")));
    }

    public static boolean isFileLogEnabled() {
        return getInstance().mFileLogEnabled;
    }

    public static void v(String format, Object... args) {
        if (DEBUG) {
            Applog log = getInstance();
            String message = log.buildMessage(format, args);
            if (log.mFileLogEnabled && log.mLogger != null) {
                message = "[V]" + message;
                log.mLogger.info(message);
                Log.v(TAG, message);
            } else {
                Log.v(TAG, message);
            }
        }
    }

    public static void d(String format, Object... args) {
        Applog log = getInstance();
        String message = log.buildMessage(format, args);
        if (log.mFileLogEnabled && log.mLogger != null) {
            message = "[D]" + message;
            log.mLogger.info(message);
            Log.d(TAG, message);
        } else {
            Log.d(TAG, message);
        }
    }

    public static void e(String format, Object... args) {
        Applog log = getInstance();
        String message = log.buildMessage(format, args);
        if (log.mFileLogEnabled && log.mLogger != null) {
            message = "[E]" + message;
            log.mLogger.severe(message);
            Log.e(TAG, message);
        } else {
            Log.e(TAG, message);
        }
    }

    public static void e(Throwable tr, String format, Object... args) {
        Applog log = getInstance();
        String message = log.buildMessage(format, args);
        if (log.mFileLogEnabled && log.mLogger != null) {
            message = "[E]" + message;
            log.mLogger.severe(message);
            Log.e(TAG, message);
        } else {
            Log.e(TAG, message, tr);
        }
    }

    public static void asserting(Exception ex, String message) {
        if (DEBUG) {
            Applog log = getInstance();
            String msg = log.buildMessage(message);
            if (ex == null) ex = new Exception(message);

            e(ex, msg);

            if (log.mFileLogEnabled && log.mLogger != null) {
                log.mLogger.log(Level.SEVERE, "[E]" + msg, ex);
            }
        }

        boolean ASSERT = false;
        if (ASSERT) {
            throw new IllegalStateException(message);
        }
    }

    private static Applog getInstance() {
        if (sInstance == null) {
            sInstance = new Applog();
        }

        return sInstance;
    }

    private String buildMessage(String format, Object... args) {
        if (mFileLogEnabled) {
            String msg = (args == null || args.length == 0) ? format : String.format(Locale.US, format, args);

            StringBuilder caller = new StringBuilder();

            try {
                // Walk up the stack looking for the first caller outside.
                // It will be at least two frames up, so start there.
                StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();
                StackTraceElement element = trace[2];
                String clsName = element.getClassName();
                caller.append(clsName.substring(clsName.lastIndexOf('.') + 1));
                caller.append(".");
                caller.append(element.getMethodName());
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, e.toString());
                caller.append("<unknown>");
            }

            mDate.setTime(System.currentTimeMillis());

            return String.format(Locale.US, " %s:[%d] %s: %s",
                    mFormatter.format(mDate), Thread.currentThread().getId(), caller.toString(), msg);

        } else {
            try {
                return String.format(Locale.US, format, args);
            } catch (UnknownFormatConversionException e) {
                e.printStackTrace();
                return String.format(Locale.US, "format:%s, %s", format, e.toString());
            } catch (MissingFormatArgumentException e) {
                e.printStackTrace();
                return String.format(Locale.US, "format:%s, %s", format, e.toString());
            }
        }
    }
}
