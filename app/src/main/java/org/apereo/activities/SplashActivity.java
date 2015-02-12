package org.apereo.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieSyncManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.apereo.App;
import org.apereo.R;
import org.apereo.constants.AppConstants;
import org.apereo.deserializers.ConfigDeserializer;
import org.apereo.deserializers.LayoutDeserializer;
import org.apereo.models.Config;
import org.apereo.models.Folder;
import org.apereo.models.Layout;
import org.apereo.models.Portlet;
import org.apereo.services.RestApi;
import org.apereo.services.UmobileRestCallback;
import org.apereo.utils.ConfigManager;
import org.apereo.utils.LayoutManager;
import org.apereo.utils.Logger;

import java.net.CookieHandler;
import java.net.CookieManager;

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

    @Bean
    ConfigManager configManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        App.setCookieManager(new CookieManager());
        CookieHandler.setDefault(App.getCookieManager());
        getGlobalConfig();
    }

    private void getGlobalConfig() {
        if (getResources().getBoolean(R.bool.shouldUseGlobalConfig)) {
            restApi.getGlobalConfig(this, new UmobileRestCallback<String>() {

                @Override
                public void onError(Exception e, String responseBody) {
                    Logger.e(TAG, e.getMessage(), e);
                }

                @Override
                public void onSuccess(String response) {
                    // parse the response
                    Gson g = new GsonBuilder()
                            .registerTypeAdapter(Config.class, new ConfigDeserializer())
                            .create();

                    Config config = g.fromJson(response, Config.class);
                    configManager.setConfig(config);

                    if (config.isUpgradeRequired()) {
                        showErrorDialog(AppConstants.UPGRADE_REQUIRED);
                    } else if (config.isUpgradeRecommended()) {
                        showErrorDialog(AppConstants.UPGRADE_RECOMMENDED);
                    } else {
                        getAccountFeed();
                    }
                }
            });
        } else {
            getAccountFeed();
        }
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
            clearCookies();

            restApi.getMainFeed(this, new UmobileRestCallback<String>() {

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

                    if (getResources().getBoolean(R.bool.shouldUseGlobalConfig)) {
                        for (Folder folder : layout.getFolders()) {
                            for (Portlet p : folder.getPortlets()) {
                                for (String portletName : configManager.getConfig().getDisabledPortlets()) {
                                    if (p.getFName().equalsIgnoreCase(portletName)) {
                                        folder.getPortlets().remove(p);
                                    }
                                }
                            }
                        }
                    }

                    layoutManager.setLayout(layout);
                    HomePageActivity_
                            .intent(SplashActivity.this)
                            .start();
                    finish();
                }
            });
        }
    }

    private void clearCookies() {
        App.getCookieManager().getCookieStore().removeAll();
        android.webkit.CookieManager.getInstance().removeAllCookie();
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
            case AppConstants.ERROR_GETTING_CONFIG:
                builder.setMessage(getString(R.string.config_unavailable));
                dialog = builder.setCancelable(false)
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .create();
                break;
            case AppConstants.UPGRADE_REQUIRED:
                builder.setTitle(R.string.upgrade_required_title);
                builder.setMessage(getString(R.string.upgrade_required));
                dialog = builder.setCancelable(false)
                        .setPositiveButton(R.string.dialog_play_store, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("market://details?id="
                                            + getApplicationContext().getPackageName()));
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    Logger.d(TAG, e.getMessage());
                                    finish();
                                }
                            }
                        })
                        .setNegativeButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .create();
                break;
            case AppConstants.UPGRADE_RECOMMENDED:
                builder.setTitle(R.string.upgrade_recommended_title);
                builder.setMessage(getString(R.string.upgrade_recommended));
                dialog = builder.setCancelable(false)
                        .setPositiveButton(R.string.dialog_play_store, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("market://details?id="
                                            + getApplicationContext().getPackageName()));
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    Logger.d(TAG, e.getMessage());
                                    finish();
                                }
                            }
                        })
                        .setNegativeButton(R.string.dialog_later, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getAccountFeed();
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
