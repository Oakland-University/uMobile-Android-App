package org.apereo.activities;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.res.ColorRes;
import org.apache.commons.lang.StringUtils;
import org.apereo.R;

/**
 * Created by schneis on 8/27/14.
 */
@EActivity
public class BaseActivity extends AppCompatActivity {

    private final String TAG = BaseActivity.class.getName();

    @ColorRes(R.color.theme_accent)
    int themeAccent;

    AlertDialog dialog;

    @UiThread
    public void showSnackBar(Context context, String msg) {
        View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar
                .make(rootView, R.string.error_title, Snackbar.LENGTH_LONG)
                .setText(msg)
                .show();
    }

    @UiThread
    public void showSnackBarWithAction(Context context, String msg, View.OnClickListener listener, String actionLabel) {
        View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar
                .make(rootView, R.string.error_title, Snackbar.LENGTH_LONG)
                .setText(msg)
                .setAction(actionLabel, listener)
                .setActionTextColor(themeAccent)
                .show();
    }

    @UiThread
    public void showSpinner(String msg) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        alertBuilder.setMessage((!StringUtils.isEmpty(msg)) ? msg : null);
        dialog = alertBuilder.show();
    }

    @UiThread
    public void dismissSpinner() {
        try {
            dialog.dismiss();
        } catch (Exception e) { }
    }

}