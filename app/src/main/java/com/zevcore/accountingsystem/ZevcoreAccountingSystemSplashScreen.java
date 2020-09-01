package com.zevcore.accountingsystem;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Observable;
import java.util.Observer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Main class for splash screen.
 */
public class ZevcoreAccountingSystemSplashScreen extends AppCompatActivity implements Observer {
    private ProgressBar progressBar;
    private EditText urlText;
    private NetworkChangeReceiver networkChangeReceiverSplashScreen = new NetworkChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        progressBar = findViewById(R.id.progressBar);
        urlText = findViewById(R.id.zevcoreUrl);
        urlText.setOnEditorActionListener((textView, i, keyEvent) -> {
            Intent mainIntent = new Intent(ZevcoreAccountingSystemSplashScreen.this, MainActivity.class);
            mainIntent.putExtra("Load-url", textView.getText().toString());
            startActivity(mainIntent);
            animateSlideLeft(this);
            finish();
            return true;
        });
        progressBar.setVisibility(View.VISIBLE);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        ObservableObject.getInstance().addObserver(this);
        registerReceiver(networkChangeReceiverSplashScreen, intentFilter);
    }

    /**
     * Transition from Left to Right
     *
     * @param context
     */
    public void animateSlideLeft(Context context) {
        ((ZevcoreAccountingSystemSplashScreen) context).overridePendingTransition(R.anim.animate_slide_left_enter, R.anim.animate_slide_left_exit);
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
                setProgressBar();
                showURLText();
            }
        }
    }

    /**
     *
     */
    private void showURLText() {
        String prefName = this.getResources().getString(R.string.zevcore_accounting_user_prefs);
        SharedPreferences sharedPreferences = this.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        if (sharedPreferences.contains("URL")) {
            Intent mainIntent = new Intent(ZevcoreAccountingSystemSplashScreen.this, MainActivity.class);
            startActivity(mainIntent);
            animateSlideLeft(this);
            finish();
        } else {
            urlText.setVisibility(View.VISIBLE);
        }
    }

    /**
     *
     */
    private void setProgressBar() {
        if (View.VISIBLE == progressBar.getVisibility()) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
