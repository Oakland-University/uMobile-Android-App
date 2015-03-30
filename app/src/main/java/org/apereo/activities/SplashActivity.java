package org.apereo.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

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

import java.net.CookieHandler;
import java.net.CookieManager;

import me.drakeet.materialdialog.MaterialDialog;

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
        manageCookies();
        getGlobalConfig();
        getAccountFeed();
    }

    private void manageCookies() {
        App.setCookieManager(new CookieManager());
        CookieHandler.setDefault(App.getCookieManager());
    }

    private void getGlobalConfig() {
        if (getResources().getBoolean(R.bool.shouldUseGlobalConfig)) {
            restApi.getGlobalConfig(this, new UmobileRestCallback<String>() {

                @Override
                public void onError(Exception e, String responseBody) {
                    showErrorDialog(AppConstants.ERROR_GETTING_CONFIG);
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
                    }
                }
            });
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
    }

    @UiThread
    protected void showErrorDialog(int msgId) {
        MaterialDialog dialog = new MaterialDialog(this)
                .setTitle(getString(R.string.error_title));
        View.OnClickListener positiveAction = null;
        View.OnClickListener negativeAction = null;
        switch (msgId) {
            case AppConstants.ERROR_GETTING_FEED:
                positiveAction = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { finish(); }
                };
                dialog = setUpDialog(dialog,
                        null, getString(R.string.error_network_connection),
                        getString(R.string.dialog_ok), positiveAction, null, null);
                break;
            case AppConstants.ERROR_GETTING_CONFIG:
                positiveAction = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { finish(); }
                };
                dialog = setUpDialog(dialog,
                        null, getString(R.string.config_unavailable),
                        getString(R.string.dialog_ok), positiveAction, null, null);
                break;
            case AppConstants.UPGRADE_REQUIRED:
                positiveAction = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            openPlayStore();
                        } catch (ActivityNotFoundException e) {
                            finish();
                        }
                    }
                };
                negativeAction = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { finish(); }
                };
                dialog = setUpDialog(dialog,
                        getString(R.string.upgrade_required_title), getString(R.string.upgrade_required),
                        getString(R.string.dialog_play_store), positiveAction,
                        getString(R.string.dialog_close), negativeAction);
                break;
            case AppConstants.UPGRADE_RECOMMENDED:
                positiveAction = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            openPlayStore();
                        } catch (ActivityNotFoundException e) {
                            finish();
                        }
                    }
                };
                negativeAction = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getAccountFeed();
                    }
                };
                dialog = setUpDialog(dialog,
                        getString(R.string.upgrade_recommended_title), getString(R.string.upgrade_recommended),
                        getString(R.string.dialog_play_store), positiveAction,
                        getString(R.string.dialog_later), negativeAction);
                break;
            default:
                break;
        }

        dialog.show();
    }

    private void openPlayStore() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id="
                + getApplicationContext().getPackageName()));
        startActivity(intent);
    }

    private MaterialDialog setUpDialog(MaterialDialog dialog,
                                       String title, String message,
                                       String positiveTitle, View.OnClickListener positiveAction,
                                       String negativeTitle, View.OnClickListener negativeAction) {
        if (title != null) {
            dialog.setTitle(title);
        }
        if (message != null) {
            dialog.setMessage(message);
        }
        if (positiveTitle != null && positiveAction != null) {
            dialog.setPositiveButton(positiveTitle, positiveAction);
        }
        if (negativeTitle != null && negativeAction != null) {
            dialog.setNegativeButton(negativeTitle, negativeAction);
        }
        return dialog;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
    }

}
