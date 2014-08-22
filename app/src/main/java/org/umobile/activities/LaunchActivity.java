package org.umobile.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.umobile.activities.HomePage_;
import org.umobile.deserializers.LayoutDeserializer;
import org.umobile.models.Layout;
import org.umobile.services.RestApi;
import org.umobile.services.UmobileRestCallback;
import org.umobile.utils.LayoutManager;

/**
 * Created by schneis on 8/20/14.
 */
@EActivity
public class LaunchActivity extends Activity{

    private final String TAG = LaunchActivity.class.getName();
    @Bean
    RestApi restApi;

    @Bean
    LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restApi.getMainFeed(new UmobileRestCallback<String>() {

            @Override
            public void onError(Exception e, String responseBody) {
                Log.e(TAG, e.getMessage(), e);
            }

            @Override
            public void onSuccess(String response) {
                // parse the response
                Gson g = new GsonBuilder()
                        .registerTypeAdapter(Layout.class, new LayoutDeserializer())
                        .create();

                Layout layout = g.fromJson(response, Layout.class);
                layoutManager.setLayout(layout);
                Log.d(TAG, " first name = " + layout.getFolders().get(0).getName());
                HomePage_
                        .intent(LaunchActivity.this)
                        .start();
                finish();
            }
        });
    }
}
