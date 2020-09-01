package com.zevcore.accountingsystem;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.zevcore.accountingsystem.location.LocationCoords;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

/**
 * Java Interface class for handling javascript function from the website
 */
public class JavascriptInterface {
    public static String FILENAME = "";
    private static LocationCoords locationCoords;
    private Context context;

    public JavascriptInterface(Context context) {
        this.context = context;
    }

    public static void setLocationForJavascriptInterface(double latitude, double longitude) {
        locationCoords = new LocationCoords();
        locationCoords.setLatitutde(latitude);
        locationCoords.setLongitude(longitude);
    }

    private static LocationCoords getLocationCoords() {
        if (null == locationCoords) {
            return new LocationCoords();
        }
        return locationCoords;
    }

    /**
     * Get String url from Blob file type using XHR
     *
     * @param blobUrl
     * @return
     */
    public static String getBase64StringFromBlobUrl(String blobUrl) {
        if (blobUrl.startsWith("blob")) {
            return "javascript: var xhr = new XMLHttpRequest();" +
                    "xhr.open('GET', '" + blobUrl + "', true);" +
                    "xhr.responseType = 'blob';" +
                    "xhr.onload = function(e) {" +
                    "    if (this.status == 200) {" +
                    "        var blobFile = this.response;" +
                    "        var reader = new FileReader();" +
                    "        reader.readAsDataURL(blobFile);" +
                    "        reader.onloadend = function() {" +
                    "            base64data = reader.result;" +
                    "            Android.getBase64FromBlobData(base64data, xhr.getResponseHeader(\"Content-Type\"));" +
                    "        }" +
                    "    }" +
                    "};" +
                    "xhr.send();";
        }
        return "javascript: alert('File : Zevcore Accounting System " + FILENAME + " Cannot be downloaded);";
    }

    @android.webkit.JavascriptInterface
    public void getBase64FromBlobData(String base64Data, String contentType) throws IOException {
        convertBase64StringAndStoreIt(base64Data, contentType);
    }

    @android.webkit.JavascriptInterface
    public void checkAndSaveDetails(String inUserName, String inPassword) {
        if (!TextUtils.isEmpty(inUserName) && !TextUtils.isEmpty(inPassword)) {
            String prefName = context.getResources().getString(R.string.zevcore_accounting_user_prefs);
            SharedPreferences sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
            if (!sharedPreferences.contains(inUserName) || !sharedPreferences.getString(inUserName, "").equals(inPassword)) {
                SharedPreferences.Editor sharedPreferencesEdit = sharedPreferences.edit();
                sharedPreferencesEdit.clear();
                sharedPreferencesEdit.putString(inUserName, inPassword);
                sharedPreferencesEdit.apply();
            }
        }
    }

    /**
     * Download PDF and Excel file from the base64 String Path decoded.
     *
     * @param base64PDf
     * @param contentType
     * @throws IOException
     */
    private void convertBase64StringAndStoreIt(String base64PDf, String contentType) throws IOException {
        final int notificationId = 1;
        final String NOTIFICATION_CHANNEL_ID = "MY_DL";
        final File dwldsPath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + "/" + getFileName(FILENAME) + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType));
        byte[] fileAsBytes = Base64.decode(base64PDf.replaceFirst("^data:" + contentType + ";base64,", ""), 0);
        FileOutputStream outputStream = new FileOutputStream(dwldsPath, false);
        outputStream.write(fileAsBytes);
        outputStream.flush();

        if (dwldsPath.exists() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (null != notificationManager) {
                disableFileURIExposure();
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Zevcore Accounting System Notifications", NotificationManager.IMPORTANCE_HIGH);
                // Configure the notification channel.
                notificationChannel.setDescription("Zevcore Accounting System Channel description");
                notificationChannel.enableLights(false);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(false);
                notificationManager.createNotificationChannel(notificationChannel);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(androidx.core.R.drawable.notification_template_icon_bg)
                        .setContentTitle(getFileName(FILENAME))
                        .setContentText(FILENAME + " List");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(dwldsPath), contentType);
                Intent chooser = Intent.createChooser(intent, context.getResources().getString(R.string.app_name));
                if (null != chooser) {
                    PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                            chooser, PendingIntent.FLAG_CANCEL_CURRENT);
                    builder.setContentIntent(contentIntent);
                }
                notificationManager.notify(notificationId, builder.build());
                Toast.makeText(context, "File : " + getFileName(FILENAME) + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType) + " is downloading", Toast.LENGTH_SHORT).show();
                JavascriptInterface.FILENAME = "";
            }
        }
    }

    /**
     * Get Filename for download
     *
     * @param fileName
     * @return
     */
    @NonNull
    private String getFileName(String fileName) {
        return "Zevcore Accounting System" + " " + fileName;
    }

    /**
     * If your app targets API 24+, and you still want/need to use file:// intents,
     * you can use hacky way to disable the runtime check
     */
    private void disableFileURIExposure() {
        try {
            Method method = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
            method.invoke(null);
        } catch (Exception e) {
            JavascriptInterface.FILENAME = "";
            Toast.makeText(context, "File Cannot be downloaded", Toast.LENGTH_SHORT).show();
        }
    }
}
