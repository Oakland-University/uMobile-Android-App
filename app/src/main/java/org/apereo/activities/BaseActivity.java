package org.apereo.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.apache.commons.lang.StringUtils;
import org.apereo.R;
import org.apereo.utils.Logger;

/**
 * Created by schneis on 8/27/14.
 */
@EActivity
public class BaseActivity extends ActionBarActivity {

    private final String TAG = BaseActivity.class.getName();

    ProgressDialog dialog;

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
    public void showShortToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @UiThread
    public void showLongToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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

        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
        }
        dialog.show();
        if (StringUtils.isEmpty(msg)) {
            dialog.setContentView(R.layout.spinner);
        }
        else {
            dialog.setMessage(msg);
        }


    }

    @UiThread
    public void dismissSpinner() {
        if (dialog != null)
            try {
                dialog.dismiss();
            } catch (Exception e) {
                Logger.d(TAG, e.getMessage());
            }
    }
}