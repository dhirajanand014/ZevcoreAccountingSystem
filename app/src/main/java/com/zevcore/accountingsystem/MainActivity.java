package com.zevcore.accountingsystem;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.zevcore.accountingsystem.helper.ZevcoreAccountingSystemHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Main Activity class
 */
public class MainActivity extends AppCompatActivity implements Observer {
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int FILECHOOSER_RESULTCODE = 1;
    private String URL;
    private String downloadUrl = "";
    public static final int REQUEST_CODE = 111;
    private static final int PERMISSION_ID = 44;
    private WebView webView;
    private int count;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private Context context = this;
    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    private ZevcoreAccountingSystemHelper zevcoreAccountingSystemHelper;
    private String ORIGINAL_URL = "";
    private boolean FROM_UI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        URL = getIntent().getStringExtra("Load-url");
        webView = findViewById(R.id.asiangis);
        swipeRefreshLayout = findViewById(R.id.swipeLayout);
        zevcoreAccountingSystemHelper = new ZevcoreAccountingSystemHelper();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setInitialScale(1);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        ObservableObject.getInstance().reset();
        ObservableObject.getInstance().addObserver(this);
        registerReceiver(networkChangeReceiver, intentFilter);
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //loads from cache or looks up to the network.
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        }
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.addJavascriptInterface(new JavascriptInterface(this), "Android");
        webView.getSettings().setUseWideViewPort(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                // Double check that we don't have any existing callbacks
                boolean allowRead = true;
                if (hasPermissions(context, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showFileChooser(filePathCallback);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                    allowRead = false;
                }
                return allowRead;
            }

            // openFileChooser for Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                // Create AndroidExampleFolder at sdcard
                // Create AndroidExampleFolder at sdcard
                File imageStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES)
                        , "Zevcore Accounting System");
                if (!imageStorageDir.exists()) {
                    // Create AndroidExampleFolder at sdcard
                    imageStorageDir.mkdirs();
                }
                // Create camera captured image file path and name
                File file = new File(
                        imageStorageDir + File.separator + "IMG_"
                                + String.valueOf(System.currentTimeMillis())
                                + ".jpg");
                mCapturedImageURI = Uri.fromFile(file);
                // Camera capture image intent
                final Intent captureIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                Intent theIntent = new Intent(Intent.ACTION_GET_CONTENT);
                theIntent.addCategory(Intent.CATEGORY_OPENABLE);
                theIntent.setType("image/*");
                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(theIntent, "Image Chooser");
                // Set camera intent to file chooser
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                        , new Parcelable[]{captureIntent});
                // On select image call onActivityResult method of activity
                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
            }

            // openFileChooser for Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            //openFileChooser for other Android versetCacheModesions
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType,
                                        String capture) {
                openFileChooser(uploadMsg, acceptType);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                try {
                    //stop url loading.
                    webView.stopLoading();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Please check your connectivity!! :" + e,
                            Toast.LENGTH_LONG).show();
                }
                super.onReceivedError(view, request, error);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (zevcoreAccountingSystemHelper.isUrlToUnload(url)) {
                    swipeRefreshLayout.setEnabled(false);
                } else {
                    swipeRefreshLayout.setEnabled(true);
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                ProgressBar progressBar = findViewById(R.id.progressBarWebView);
                if (View.VISIBLE == progressBar.getVisibility()) {
                    progressBar.setVisibility(View.GONE);
                    view.setAlpha(1);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                swipeRefreshLayout.setRefreshing(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    view.evaluateJavascript("javascript:var printButton = document.getElementsByClassName('buttons-print');" +
                            "                                  var csvButton = document.getElementsByClassName('buttons-csv');" +
                            "                                       if(printButton.length > 0 && csvButton.length > 0){" +
                            "                                            printButton[0].style.display = csvButton[0].style.display = 'none';" +
                            "                                       }", null);
                    if (URL.equals(url)) {
                        zevcoreAccountingSystemHelper.autoLoginIntoGIS(view.getContext(), view);
                    }
                }
            }

        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                downloadUrl = url;
                if (hasPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    JavascriptInterface.FILENAME = fetchFileName();
                    webView.loadUrl(JavascriptInterface.getBase64StringFromBlobUrl(downloadUrl));
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
            }
        });
        webView.setLayerType(Build.VERSION.SDK_INT >= 19 ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_SOFTWARE, null);
        if (savedInstanceState == null) {
            webView.loadUrl(URL);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    /**
     * Fetch file name from the URL to be used to create PDF or Excel frile with the Filename.
     *
     * @return
     */
    private String fetchFileName() {
        String[] tableName = webView.getUrl().split(URL);
        for (String fileURL : tableName) {
            if (fileURL.contains(".php")) {
                String fileNameSplit = fileURL.split(".php")[0];
                return fileNameSplit.substring(0, 1).toUpperCase() + fileNameSplit.substring(1);
            }
        }
        return "";
    }

    /**
     * File chooser for camera to upload profile image.
     *
     * @param filePathCallback
     */
    private void showFileChooser(ValueCallback<Uri[]> filePathCallback) {
        if (mFilePathCallback != null) {
            mFilePathCallback.onReceiveValue(null);
        }
        mFilePathCallback = filePathCallback;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("Can't create Image File", ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
            } else {
                takePictureIntent = null;
            }
        }
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");
        Intent[] intentArray;
        if (takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }
        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            if (zevcoreAccountingSystemHelper.isUrlToUnload(webView.getUrl())) {
                swipeRefreshLayout.setEnabled(false);
            } else {
                swipeRefreshLayout.setEnabled(true);
            }
            webView.goBack();
        } else {
            finish();
        }
    }

    /**
     * Check Runtime Permissions.
     *
     * @param context
     * @param permissions
     * @return
     */
    private boolean hasPermissions(Context context, String... permissions) {
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }
        }
        return listPermissionsNeeded.isEmpty();
    }

    /**
     * Check Runtime Permissions.
     *
     * @param context
     * @param permissions
     */
    private boolean retrieveLocationPermissions(Context context, String... permissions) {
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }
        }
        return listPermissionsNeeded.isEmpty();
    }

    /**
     * Allow or Deny result handler method.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            JavascriptInterface.FILENAME = fetchFileName();
            webView.loadUrl(JavascriptInterface.getBase64StringFromBlobUrl(downloadUrl));
        } else if (requestCode == 2 && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showFileChooser(null);
        }
    }

    /**
     * Create an image file to get uploaded to the profile.
     *
     * @return
     * @throws IOException
     */

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObservableObject.getInstance().deleteObserver(this);
        unregisterReceiver(networkChangeReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] results = null;
            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK && data == null) {
                // If there is not data, then we may have taken a photo
                if (mCameraPhotoPath != null) {
                    results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                }
            } else {
                if (data != null && data.getDataString() != null) {
                    results = new Uri[]{Uri.parse(data.getDataString())};
                }
            }
            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == this.mUploadMessage) {
                    return;
                }
                Uri result = null;
                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    } else {
                        // retrieve from the private variable if the intent is null
                        result = data == null ? mCapturedImageURI : data.getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "activity :" + e,
                            Toast.LENGTH_LONG).show();
                }
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
    }

    @Override
    public void update(Observable observable, Object intent) {
        if (null != intent) {
            boolean status = ((Intent) intent).getBooleanExtra("status", Boolean.TRUE);
            String message = getSnackBarMessage(status);
            if (!message.isEmpty()) {
                Snackbar snackBar = Snackbar.make(findViewById(android.R.id.content), message, status ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_INDEFINITE);
                // get snackbar view
                View mView = snackBar.getView();
                Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) mView;

                layout.setPadding(0, 0, 0, 0);//set padding to 0
                // get textview inside snackbar view
                TextView mTextView = mView.findViewById(com.google.android.material.R.id.snackbar_text);
                // set text to center
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                } else {
                    mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                }
                mTextView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, status ? R.color.colorGreen : R.color.design_default_color_error));
                // show the snackbar
                snackBar.show();
            }
        }
    }

    /**
     * Update Snackbar message on netword state change.
     *
     * @param inStatus
     * @return
     */
    private String getSnackBarMessage(boolean inStatus) {
        if (inStatus) {
            if (!ORIGINAL_URL.isEmpty()) {
                webView.loadUrl(ORIGINAL_URL);
                ORIGINAL_URL = "";
                return "Connected to internet.";
            }
            return "";
        }
        displayErrorImage();
        return "Check internet connection!!";
    }

    /**
     * Load image as url when no internet connection is availible and changes to load failed url back when internet is availaible.
     */
    private void displayErrorImage() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        ORIGINAL_URL = webView.getUrl();
        String html = "<html><head><title>Example</title><meta name=\"viewport\"\"content=\"width=" + width + ", initial-scale=0.65 \" /></head>";
        html += "<body><img width=\"" + width + "\" src=\"" + "screen.png" + "\" /></body></html>";
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.loadDataWithBaseURL("file:///android_res/drawable/", html, "text/html", "UTF-8", null);
    }
}
