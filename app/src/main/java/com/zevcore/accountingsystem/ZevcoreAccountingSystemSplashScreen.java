package com.zevcore.accountingsystem;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.zevcore.accountingsystem.helper.ZevcoreAccountingSystemHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

/**
 * Main class for splash screen.
 */
public class ZevcoreAccountingSystemSplashScreen extends AppCompatActivity implements Observer {
    private ProgressBar progressBar;
    private AppCompatEditText urlText;
    private AppCompatButton nextButton;
    private ZevcoreAccountingSystemHelper zevcoreAccountingSystemHelper = new ZevcoreAccountingSystemHelper();
    private NetworkChangeReceiver networkChangeReceiverSplashScreen = new NetworkChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        progressBar = findViewById(R.id.progressBar);
        urlText = findViewById(R.id.zevCoreUrl);
        nextButton= findViewById(R.id.zevCoreUrlButton);
        urlText.setOnEditorActionListener((textView, i, keyEvent) -> {
            startWebView(textView);
            return true;
        });
        progressBar.setVisibility(View.VISIBLE);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        ObservableObject.getInstance().addObserver(this);
        registerReceiver(networkChangeReceiverSplashScreen, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObservableObject.getInstance().deleteObserver(this);
        unregisterReceiver(networkChangeReceiverSplashScreen);
    }

    @Override
    public void update(Observable observable, Object intent) {
        if (null != intent) {
            boolean status = ((Intent) intent).getBooleanExtra("status", Boolean.TRUE);
            Snackbar snackBar = Snackbar.make(findViewById(android.R.id.content), status ? "Connected to internet." : "Check internet connection!!", status ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_INDEFINITE);
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
            mTextView.setBackgroundColor(ContextCompat.getColor(ZevcoreAccountingSystemSplashScreen.this, status ? R.color.colorGreen : R.color.design_default_color_error));
            // show the snackbar
            snackBar.show();

            if (status) {
                zevcoreAccountingSystemHelper.setProgressBar(this, progressBar, urlText, nextButton);
            } else {
                zevcoreAccountingSystemHelper.resetToProgressBar(urlText, progressBar, nextButton);
            }
        }
    }

    /**
     * @param view
     */
    public void loadWebView(View view) {
        if (urlText.length() <= 0 || !zevcoreAccountingSystemHelper.isValidURL(urlText.getText().toString())) {
            urlText.requestFocus();
            urlText.setError("Please enter a valid url");
        } else {
            startWebView(urlText);
        }
    }

    /**
     * @param textView
     */
    private void startWebView(TextView textView) {
        Map<String, String> activityExtraMaps = new HashMap<>();
        activityExtraMaps.put("Load-url", textView.getText().toString());
        zevcoreAccountingSystemHelper.startMainActivity(this, activityExtraMaps);
    }
}
