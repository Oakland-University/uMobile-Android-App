package org.apereo.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.apereo.App;
import org.apereo.R;
import org.apereo.constants.AppConstants;
import org.apereo.deserializers.LayoutDeserializer;
import org.apereo.models.Layout;
import org.apereo.services.RestApi;
import org.apereo.services.UmobileRestCallback;
import org.apereo.utils.LayoutManager;
import org.apereo.utils.Logger;

/**
 * Created by schneis on 8/27/14.
 */
@EActivity(R.layout.activity_splash)
public class SplashActivity extends BaseActivity {

    private final String TAG = SplashActivity.class.getName();

    private final String ACCOUNT_TYPE = App.getInstance().getResources().getString(R.string.account_type);

    @Bean
    RestApi restApi;

    @Bean
    LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        getAccountFeed();
    }

    private void getAccountFeed() {
        AccountManager accountManager = AccountManager.get(App.getInstance());
        if (accountManager.getAccountsByType(ACCOUNT_TYPE).length != 0) {
            Account account = accountManager.getAccountsByType(ACCOUNT_TYPE)[0];
            LoginActivity_
                    .intent(SplashActivity.this)
                    .url(getResources().getString(R.string.login_url))
                    .username(account.name)
                    .password(accountManager.getPassword(account))
                    .start();
        } else {
            restApi.getMainFeed(new UmobileRestCallback<String>() {

                @Override
                public void onBegin() {
                                    super.onBegin();
                                                    }

                @Override
                public void onError(Exception e, String responseBody) {
                    Logger.e(TAG, e.getMessage(), e);
                    showErrorDialog(AppConstants.ERROR_GETTING_FEED);
                }

                @Override
                public void onSuccess(String response) {
                    // parse the response
                    Gson g = new GsonBuilder()
                            .registerTypeAdapter(Layout.class, new LayoutDeserializer())
                            .create();

                    Layout layout = g.fromJson(response, Layout.class);
                    layoutManager.setLayout(layout);
                    HomePage_
                            .intent(SplashActivity.this)
                            .start();
                    finish();
                }
            });
        }
    }

    @UiThread
    protected void showErrorDialog(int msgId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher);
        Dialog dialog = builder.setTitle(getString(R.string.error_title)).create();

        switch (msgId) {
            case AppConstants.ERROR_GETTING_FEED:
                builder.setMessage(getString(R.string.error_network_connection));
                dialog = builder.setCancelable(false)
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .create();
                break;

            default:
                break;
        }

        dialog.show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
    }

}
