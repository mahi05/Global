package com.mahii.Global;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AppMethod {

    /* For Jackson */
    static ObjectMapper mapper = null;
    static Lock lock = new ReentrantLock();

    /* Make TextView UnderLined*/
    public static void makeTextUnderLined(String data, TextView view) {

        SpannableString content = new SpannableString(data);
        content.setSpan(new UnderlineSpan(), 0, data.length(), 0);
        view.setText(content);

    }

    /*
     * Refer to http://stackoverflow.com/a/2560017/1100536
     * Split words as Camel Case i.e.,MyName will split into "My Name"
     */
    public static String splitCamelCase(String s) {
        return s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
    }

    /* Set Font Method */
    public static void overrideFonts(final Context context, final View v) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child);
                }
            } else if (v instanceof TextView) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/medium.otf"));
            }
        } catch (Exception ignored) {
        }
    }

    /* Start Jackson Library Mapper */
    public static synchronized ObjectMapper getMapper() {

        if (mapper != null) {
            return mapper;
        }
        try {
            lock.lock();
            if (mapper == null) {
                mapper = new ObjectMapper();
                mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES,
                        false);
            }
            lock.unlock();
        } catch (Exception e) {
            if (e != null)
                Log.e("Mapper", "Mapper Initialization Failed. Exception :: "
                        + e.getMessage());
        }

        return mapper;
    }
    /* END */

    /* Play Music File From Raw Directory in Media Player */
    public static MediaPlayer buildMediaPlayer(Context activity, int musicId) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // When the beep has finished playing, rewind to queue up another one.
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer player) {
                player.seekTo(0);
            }
        });

        AssetFileDescriptor file = activity.getResources().openRawResourceFd(musicId);
        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            file.close();
            mediaPlayer.setVolume(AppConstant.BEEP_VOLUME, AppConstant.BEEP_VOLUME);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ioe) {
            mediaPlayer = null;
        }
        return mediaPlayer;
    }

    /* To show installed app details */
    public static void showInstalledAppDetails(Context context, String packageName) {
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;

        // above 2.3
        if (apiLevel >= 9) {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(AppConstant.SCHEME, packageName, null);
            intent.setData(uri);
        }

        // below 2.3
        else {
            final String appPkgName = (apiLevel == 8 ? AppConstant.APP_PKG_NAME_22 : AppConstant.APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(AppConstant.APP_DETAILS_PACKAGE_NAME, AppConstant.APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        context.startActivity(intent);
    }

    /* To check if internet connection is available or not */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isConnected = false;
        if (connectivity != null) {
            NetworkInfo nInfo = connectivity.getActiveNetworkInfo();

            if (nInfo != null && nInfo.getState() == NetworkInfo.State.CONNECTED) {
                isConnected = true;
            }
        } else {
            isConnected = false;
        }
        return isConnected;
    }

    /* Get IP of the device */
    public static String getWifiIP(Context context) {
        String result;
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo info = wifiMgr.getDhcpInfo();
        String[] temp = info.toString().split(" ");
        result = temp[1];
        return result;
    }


    /**
     * Convert a DIP value to pixel.
     *
     * @param context
     * @param dip     dip value
     * @return pixel value which is calculated depending on your current device configuration
     */
    public static int dpToPx(Context context, float dip) {
        return (int) (dip * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * Convert a pixel value to sp.
     */
    public static float pxToSp(Context context, float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px / scaledDensity;
    }

    /**
     * Convert a sp value to px.
     */
    public static int spToPx(Context context, int sp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics()) + 0.5f);
    }


    /* To get more details about an exception that occurred in an application */
    private static String getExceptionDetails(Activity act, Exception e) {
        StackTraceElement[] stackTraceElement = e.getStackTrace();

        String fileName = "";
        String methodName = "";
        int lineNumber = 0;

        try {
            String packageName = act.getApplicationInfo().packageName;
            int i = 0;
            while (i < stackTraceElement.length) {
                if (stackTraceElement[i].getClassName().startsWith(packageName)) {
                    fileName = stackTraceElement[i].getFileName();
                    methodName = stackTraceElement[i].getMethodName();
                    lineNumber = stackTraceElement[i].getLineNumber();
                    break;
                }
                i++;
            }
        } catch (Exception ignored) {
        }

        return fileName + ":" + methodName + "():line " + String.valueOf(lineNumber);
    }

    /**
     * START Returns the consumer friendly device name
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }
    /* END Returns the consumer friendly device name */


    /* [START]
     * Converts milliseconds to "x days, x hours, x mins, x secs"
     *
     * @param millis     The milliseconds
     * @param longFormat {@code true} to use "seconds" and "minutes" instead of "secs" and "mins"
     * @return A string representing how long in days/hours/minutes/seconds millis is.
     */
    public static String millisToString(long millis, boolean longFormat) {
        if (millis < 1000) {
            return String.format("0 %s", longFormat ? "seconds" : "secs");
        }
        String[] units = {
                "day", "hour", longFormat ? "minute" : "min", longFormat ? "second" : "sec"
        };
        long[] times = new long[4];
        times[0] = TimeUnit.DAYS.convert(millis, TimeUnit.MILLISECONDS);
        millis -= TimeUnit.MILLISECONDS.convert(times[0], TimeUnit.DAYS);
        times[1] = TimeUnit.HOURS.convert(millis, TimeUnit.MILLISECONDS);
        millis -= TimeUnit.MILLISECONDS.convert(times[1], TimeUnit.HOURS);
        times[2] = TimeUnit.MINUTES.convert(millis, TimeUnit.MILLISECONDS);
        millis -= TimeUnit.MILLISECONDS.convert(times[2], TimeUnit.MINUTES);
        times[3] = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS);
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (times[i] > 0) {
                s.append(String.format("%d %s%s, ", times[i], units[i], times[i] == 1 ? "" : "s"));
            }
        }
        return s.toString().substring(0, s.length() - 2);
    }

    /**
     * Converts milliseconds to "x days, x hours, x mins, x secs"
     *
     * @param millis The milliseconds
     * @return A string representing how long in days/hours/mins/secs millis is.
     */
    public static String millisToString(long millis) {
        return millisToString(millis, false);
    }
    /* [END] */

    /* [START ENCODE AND DECODE] */
    public static String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }

    public static Bitmap decodeFromBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
    /* [END ENCODE AND DECODE] */

    /* START Show OK Dialog */
    public static void showOkDialog(Context context, String messageText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(messageText);
        builder.setCancelable(true);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    /* END Show OK Dialog */

    /* START Show OK Dialog With Message & Title */
    public static void showOkDialogWithMessage(String title, String msg, Activity act) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(act);
        if (title != null) {

            TextView dialogTitle = new TextView(act);
            dialogTitle.setText(title);
            dialogTitle.setPadding(10, 10, 10, 10);
            dialogTitle.setGravity(Gravity.CENTER);
            dialogTitle.setTextColor(Color.BLACK);
            dialogTitle.setTextSize(20);
            dialog.setCustomTitle(dialogTitle);

        }
        if (msg != null) {
            dialog.setMessage(msg);
        }
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dlg = dialog.show();
        TextView messageText = (TextView) dlg.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);

    }
    /* END Show OK Dialog With Message & Title */

    /* Upper Case of First Letter */
    public static String uppercaseFirstLetters(String str) {
        boolean prevWasWhiteSp = true;
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isLetter(chars[i])) {
                if (prevWasWhiteSp) {
                    chars[i] = Character.toUpperCase(chars[i]);
                }
                prevWasWhiteSp = false;
            } else {
                prevWasWhiteSp = Character.isWhitespace(chars[i]);
            }
        }
        return new String(chars);
    }
    /* END */

    /* To show full ListView Inside ScrollView */
    public static void setListViewHeightBasedOnChildren(ListView myListView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;
    
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
    
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
    /* END */

    /* START Convert InputStream to String */
    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    /* END */

    /* Start FadeIn and FadeOut Animation */
    public static void fadeIn(final View v) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(v, "alpha", 0f, 1f);
        objectAnimator.setDuration(1000L);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeOut(v);
            }
        });
        objectAnimator.start();
    }

    public static void fadeOut(final View v) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(v, "alpha", 1f, 0f);
        objectAnimator.setDuration(1000L);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeIn(v);
            }
        });
        objectAnimator.start();
    }
    /* END */

    /* Start Convert Numbers into Calc type number format */
    public static String convertToCalcFormat(String value) {

        DecimalFormat myFormatter = new DecimalFormat("###,###,###,###.###");
        String stripped1 = Double.valueOf(value).toString();
        stripped1 = myFormatter.format(Double.valueOf(stripped1));
        if (stripped1.endsWith(".0"))
            stripped1 = stripped1.substring(0, stripped1.length() - 2);
        return stripped1;
    }
    /* END */

    /* Start Check if it is a first date */
    public static boolean isFirstDate(Context context) {

        Calendar c = Calendar.getInstance();
        int date = c.get(Calendar.DATE);

        if (date == 1) {

            if (AppMethodSharedPref.getBooleanPreference(context, AppConstant.IS_TASK_UPDATED)) {
                AppMethodSharedPref.setBooleanPreference(context, AppConstant.IS_NEW_MONTH, false);
            } else {
                AppMethodSharedPref.setBooleanPreference(context, AppConstant.IS_NEW_MONTH, true);
            }

        } else {
            AppMethodSharedPref.setBooleanPreference(context, AppConstant.IS_NEW_MONTH, false);
            AppMethodSharedPref.setBooleanPreference(context, AppConstant.IS_TASK_UPDATED, false);
        }

        if (AppMethodSharedPref.getBooleanPreference(context, AppConstant.IS_NEW_MONTH)) {
            AppMethodSharedPref.setBooleanPreference(context, AppConstant.IS_TASK_UPDATED, true);
            return true;
        } else {
            return false;
        }

    }
    /* END */

    /* Start Send email */
    public static Intent getSendEmailIntent(Context context, String email, String subject, String body, String attachment) {

        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        try {

            emailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");

            emailIntent.setType("text/html");

            if (email != null)
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});

            if (subject != null)
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);

            if (body != null)
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

            if (attachment != null)
                emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(attachment));

            return emailIntent;

        } catch (Exception e) {

            emailIntent.setType("text/html");

            if (email != null)
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});

            if (subject != null)
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        subject);

            if (body != null)
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

            if (attachment != null)
                emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(attachment));

            return emailIntent;
        }
    }
    /* END */

    /* Start Get month name from the integer */
    public static String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month];
    }
    /* END */

    /* Start Get date from the current date */
    public static String getDateDiffFromCurrentDate(String dateFormat, int diff) {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, diff);

        SimpleDateFormat dateFormation = new SimpleDateFormat(dateFormat, Locale.getDefault());

        return dateFormation.format(cal.getTime());
    }
    /* END */

    /* Start Get month from the current month */
    public static String getMonthDiffFromCurrentDate(String dateFormat, int diff) {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, diff);

        SimpleDateFormat dateFormation = new SimpleDateFormat(dateFormat, Locale.getDefault());

        return dateFormation.format(cal.getTime());
    }
    /* END */

    /* Start open file from file name */
    public static void openFile(Activity activity, String filename) {

        File openFile = new File(Environment.getExternalStorageDirectory()
                + File.separator + "Test Saviour" + File.separator + filename);

        Uri uri = Uri.fromFile(openFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (openFile.toString().contains(".doc")
                || openFile.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (openFile.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (openFile.toString().contains(".ppt")
                || openFile.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (openFile.toString().contains(".xls")
                || openFile.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (openFile.toString().contains(".zip")
                || openFile.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/zip");
        } else if (openFile.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (openFile.toString().contains(".wav")
                || openFile.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (openFile.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (openFile.toString().contains(".jpg")
                || openFile.toString().contains(".jpeg")
                || openFile.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (openFile.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (openFile.toString().contains(".3gp")
                || openFile.toString().contains(".mpg")
                || openFile.toString().contains(".mpeg")
                || openFile.toString().contains(".mpe")
                || openFile.toString().contains(".mp4")
                || openFile.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            // if you want you can also define the intent type for any other
            // file

            // additionally use else clause below, to manage other unknown
            // extensions
            // in this case, Android will show all applications installed on the
            // device
            // so you can choose which application to use
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
    /* END */

    /* Start open file from Url */
    public static void openFileFromURL(Activity activity, String url) {

        Uri uri = Uri.parse(url);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (url.contains(".doc")
                || url.contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (url.contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (url.contains(".ppt")
                || url.contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (url.contains(".xls")
                || url.contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (url.contains(".zip")
                || url.contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/zip");
        } else if (url.contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (url.contains(".wav")
                || url.contains(".mp3")
                || url.contains(".m4a")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (url.contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (url.contains(".jpg")
                || url.contains(".jpeg")
                || url.contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (url.contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (url.contains(".3gp")
                || url.contains(".mpg")
                || url.contains(".mpeg")
                || url.contains(".mpe")
                || url.contains(".mp4")
                || url.contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
    /* END */

    /* START */
    /* Locks the device window in landscape mode */
    public static void lockOrientationLandscape(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /* Locks the device window in portrait mode */
    public static void lockOrientationPortrait(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /* Allows user to freely use portrait or landscape mode */
    public static void unlockOrientation(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
    /* END */

    /* START */
    /* Create db file of sqLite data in external storage */
    /* Requires WRITE_EXTERNAL_STORAGE in manifest */
    public static void writeDbToSD(Context context) throws IOException {

        String DB_PATH;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DB_PATH = context.getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator;
        } else {
            DB_PATH = context.getFilesDir().getPath() + context.getPackageName() + "/databases/";
        }

        File sd = Environment.getExternalStorageDirectory();

        if (sd.canWrite()) {
            String currentDBPath = "teezom.db";
            String backupDBPath = "backupname.db";
            File currentDB = new File(DB_PATH, currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
        }
    }
    /* END */
    
    /* START Convert date-string into Date  */
    public static Date dateFromString(String dateString, String dateFormat){

        Date date = null;
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        try {
            date = format.parse(dateString);
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    /* END */
    
    /* START Display short month name from month no */
    public static String formatMonth(String month) {
        SimpleDateFormat monthParse = new SimpleDateFormat("MM");
        SimpleDateFormat monthDisplay = new SimpleDateFormat("MMM");
        try {
            return monthDisplay.format(monthParse.parse(month));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return month;
    }
    /* END */
    
    /* START A small class that contain some static logic for setting up a random String of arbitrary length. */
	public static class RandomString {
        private static final char[] symbols = new char[36];
        private static final Random random = new Random();

        static {
            for (int idx = 0; idx < 10; ++idx) {
                symbols[idx] = (char) ('0' + idx);
            }
            for (int idx = 10; idx < 36; ++idx) {
                symbols[idx] = (char) ('a' + idx - 10);
            }
        }

        private RandomString() {
        }

        /**
         * Generate an insecure random alpha-numeric String of the length given in the constructor. Used internally
         * as an ID for separate HTTP requests to distinguish one from another when logging.
         *
         * @param length The length of the random String.
         * @return The random String.
         */
        public static String getString(int length) {
            char[] buf = new char[length];
            for (int idx = 0; idx < buf.length; ++idx) {
                buf[idx] = symbols[random.nextInt(symbols.length)];
            }
            return new String(buf);
        }
    }
    /* To use :- Log.e("MaHi", RandomString.getString(5)); */
    /* END */

}
