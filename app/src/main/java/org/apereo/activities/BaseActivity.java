package org.apereo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.ActionClickListener;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.res.ColorRes;
import org.apache.commons.lang.StringUtils;
import org.apereo.R;

/**
 * Created by schneis on 8/27/14.
 */
@EActivity
public class BaseActivity extends ActionBarActivity {

    private final String TAG = BaseActivity.class.getName();

    @ColorRes(R.color.theme_accent)
    int themeAccent;

    MaterialDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @UiThread
    public void showSnackBar(String msg) {
        SnackbarManager.show(
                Snackbar.with(this)
                        .text(msg)
                        .type(SnackbarType.MULTI_LINE)
                        .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
        );
    }

    @UiThread
    public void showSnackBarWithAction(String msg, ActionClickListener listener, String actionLabel) {
        SnackbarManager.show(
                Snackbar.with(this)
                        .text(msg)
                        .type(SnackbarType.MULTI_LINE)
                        .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                        .dismissOnActionClicked(true)
                        .actionColor(themeAccent)
                        .actionLabel(actionLabel)
                        .actionListener(listener),
                this
        );
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void showSpinner() {
        showSpinner(null);
    }

    @UiThread
    public void showSpinner(String msg) {

        dialog = new MaterialDialog.Builder(this)
                .content((!StringUtils.isEmpty(msg)) ? msg : null)
                .progress(true, 0)
                .autoDismiss(true)
                .build();
        dialog.show();

    }

    public void dismissSpinner() {
        try {
            dialog.dismiss();
        } catch (Exception e) { }
    }

}