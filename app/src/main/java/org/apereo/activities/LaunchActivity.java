package org.apereo.activities;

import android.app.Activity;
import android.os.Bundle;

import org.androidannotations.annotations.EActivity;

/**
 * Created by schneis on 8/20/14.
 */
@EActivity
public class LaunchActivity extends Activity {

    private final String TAG = LaunchActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashActivity_
                .intent(this)
                .start();
        finish();
    }
}
