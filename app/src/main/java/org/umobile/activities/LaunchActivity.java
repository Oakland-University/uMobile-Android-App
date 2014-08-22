package org.umobile.activities;

import android.app.Activity;
import android.os.Bundle;

import org.umobile.activities.HomePage_;

/**
 * Created by schneis on 8/20/14.
 */
public class LaunchActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HomePage_
            .intent(this)
            .start();
        finish();
    }
}
