package org.apereo.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.apereo.activities.HomePage_;
import org.apereo.deserializers.LayoutDeserializer;
import org.apereo.models.Layout;
import org.apereo.services.RestApi;
import org.apereo.services.UmobileRestCallback;
import org.apereo.utils.LayoutManager;

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
