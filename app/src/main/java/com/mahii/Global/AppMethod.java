package com.mahii.Global;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AppMethod {

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
    public static void showOkDialog(String title, String msg, Activity act) {
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
    /* END Show OK Dialog */

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
    public static void getListViewSize(ListView myListView) {
        ListAdapter myListAdapter = myListView.getAdapter();
        if (myListAdapter == null) {
            //do nothing return null
            return;
        }

        //set listAdapter in loop for getting final size
        int totalHeight = 0;
        for (int size = 0; size < myListAdapter.getCount(); size++) {
            View listItem = myListAdapter.getView(size, null, myListView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        //setting listview item in adapter
        ViewGroup.LayoutParams params = myListView.getLayoutParams();
        params.height = totalHeight + (myListView.getDividerHeight() * (myListAdapter.getCount() - 1));
        myListView.setLayoutParams(params);

        // print height of adapter on log
        Log.i("height of listItem:", String.valueOf(totalHeight));
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

}
