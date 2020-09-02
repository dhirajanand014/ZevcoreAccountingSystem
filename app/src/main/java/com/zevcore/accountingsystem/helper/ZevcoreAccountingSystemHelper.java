package com.zevcore.accountingsystem.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.zevcore.accountingsystem.MainActivity;
import com.zevcore.accountingsystem.R;
import com.zevcore.accountingsystem.ZevcoreAccountingSystemSplashScreen;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

@TargetApi(value = 24)
public class ZevcoreAccountingSystemHelper {

    private static final List<String> UNLOAD_REFRESH_URLS = Collections.unmodifiableList(
            Arrays.asList("user_add_location.php", "add_om.php", "add_ss.php", "add_fd.php", "usrfd_edit.php", "usrss_edit.php", "usrom_edit.php", "user_rmu.php", "user_dtr.php", "user_htpole.php", "user_ugpath.php", "user_ohline.php", "usrdtredit-details.php", "usrrmuedit-details.php", "usrhtedit-details.php", "usrugedit-details.php", "usrohtedit-details.php", "user_dashboard.php", "htedit-details.php", "rmuedit-details.php", "dtredit-details.php", "ohtedit-details.php", "ugedit-details.php", "genrate_map.php"));

    /**
     * Get the user details from the share preferences that was saved.
     *
     * @param inSharedPreferences
     * @return
     */
    public Map.Entry<String, ?> getUserDetails(SharedPreferences inSharedPreferences) {
        if (!inSharedPreferences.getAll().isEmpty()) {
            return inSharedPreferences.getAll().entrySet().stream().findFirst().orElse(null);
        }
        return null;
    }

    /**
     * Check if the URL to unload is part of the refresh URLs.
     *
     * @param inUrl
     * @return
     */
    public boolean isUrlToUnload(final String inUrl) {
        return UNLOAD_REFRESH_URLS.stream().anyMatch(string -> inUrl.contains(string));
    }

    /**
     * Auto login into GIS application if the user has already logged in.
     *
     * @param inContext
     * @param inView
     */
    public void autoLoginIntoGIS(Context inContext, WebView inView) {
        String prefName = inContext.getResources().getString(R.string.zevcore_accounting_user_prefs);
        SharedPreferences sharedPreferences = inContext.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        Map.Entry<String, ?> userDetails = getUserDetails(sharedPreferences);
        if (null != userDetails && !TextUtils.isEmpty(userDetails.getKey())) {
            ProgressBar progressBar = ((MainActivity) inContext).findViewById(R.id.progressBarWebView);
            progressBar.setVisibility(View.VISIBLE);
            inView.setAlpha(0.5f);
            inView.evaluateJavascript("javascript: var uname = document.getElementById('uname');" +
                    "uname.value = '" + userDetails.getKey() + "';" +
                    "var pd = document.getElementById('password');" +
                    "pd.value = '" + userDetails.getValue() + "';" +
                    "document.getElementById('btn-login').click();" +
                    "document.getElementById('btn-login').disabled = true;", null);
        }
    }

    /**
     * @param inUrlText
     * @param inProgressBar
     * @param nextButton
     */
    public void resetToProgressBar(AppCompatEditText inUrlText, ProgressBar inProgressBar, AppCompatButton nextButton) {
        inUrlText.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        inProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * @param inContext
     * @param inUrlText
     * @param nextButton
     * @return
     */
    private boolean showURLText(Context inContext, AppCompatEditText inUrlText, AppCompatButton nextButton) {
        String prefName = inContext.getResources().getString(R.string.zevcore_accounting_user_prefs);
        SharedPreferences sharedPreferences = inContext.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        boolean isUrlTextToBeShown;
        if (sharedPreferences.contains("URL")) {
            isUrlTextToBeShown = false;
            Map<String, String> activityExtraMaps = new HashMap<>();
            activityExtraMaps.put("Load-url", sharedPreferences.getString("URL", "DEFAULT"));
            startMainActivity(inContext, activityExtraMaps);
        } else {
            isUrlTextToBeShown = true;
            nextButton.setVisibility(View.VISIBLE);
            inUrlText.setVisibility(View.VISIBLE);
        }
        return isUrlTextToBeShown;
    }

    /**
     * @param inContext
     * @param inKeyValue
     */
    public void startMainActivity(Context inContext, Map<String, String> inKeyValue) {
        String prefName = inContext.getResources().getString(R.string.zevcore_accounting_user_prefs);
        SharedPreferences sharedPreferences = inContext.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        if (!sharedPreferences.contains("URL")) {
            SharedPreferences.Editor sharedPreferencesEdit = sharedPreferences.edit();
            sharedPreferencesEdit.clear();
            sharedPreferencesEdit.putString("URL", inKeyValue.get("Load-url"));
            sharedPreferencesEdit.apply();
        }
        Intent mainIntent = new Intent(inContext, MainActivity.class);
        if (null != inKeyValue && !inKeyValue.isEmpty()) {
            inKeyValue.entrySet().forEach(entry -> mainIntent.putExtra(entry.getKey(), entry.getValue()));
        }
        inContext.startActivity(mainIntent);
        animateSlideLeft(inContext);
        ((ZevcoreAccountingSystemSplashScreen) inContext).finish();
    }

    /**
     * Transition from Left to Right
     *
     * @param context
     */
    public void animateSlideLeft(Context context) {
        ((ZevcoreAccountingSystemSplashScreen) context).overridePendingTransition(R.anim.animate_slide_left_enter, R.anim.animate_slide_left_exit);
    }

    /**
     *
     */
    public boolean setProgressBar(Context inContext, ProgressBar inProgressBar, AppCompatEditText inEditText, AppCompatButton nextButton) {
        if (View.VISIBLE == inProgressBar.getVisibility()) {
            inProgressBar.setVisibility(View.GONE);
            return showURLText(inContext, inEditText, nextButton);
        }
        return false;
    }

    public boolean isValidURL(String urlText) {
        return Patterns.WEB_URL.matcher(urlText).matches();
    }
}
