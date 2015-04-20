package org.apereo.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
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
import java.util.ArrayList;
import java.util.List;

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

                    getAccountFeed();

                    if (config.isUpgradeRequired()) {
                        showErrorDialog(AppConstants.UPGRADE_REQUIRED);
                    } else if (config.isUpgradeRecommended()) {
                        showErrorDialog(AppConstants.UPGRADE_RECOMMENDED);
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

                    List<Portlet> portletReferences = new ArrayList<Portlet>();
                    boolean usingGlobalConfig = getResources().getBoolean(R.bool.shouldUseGlobalConfig);
                    if (usingGlobalConfig) {
                        List<String> disabledPortlets = configManager.getConfig().getDisabledPortlets();
                        for (Folder f : layout.getFolders()) {
                            for (Portlet p : f.getPortlets()) {
                                if (disabledPortlets.contains(p.getFName())) {
                                    portletReferences.add(p);
                                }
                            }
                        }
                    }

                    for (Folder f : layout.getFolders()) {
                        for (Portlet p : portletReferences) {
                            f.getPortlets().remove(p);
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
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(this)
                .title(getString(R.string.error_title));
        MaterialDialog.ButtonCallback positiveAction = null;
        MaterialDialog.ButtonCallback negativeAction = null;
        switch (msgId) {
            case AppConstants.ERROR_GETTING_FEED:
                positiveAction = new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) { finish(); }
                };
                dialog = buildDialog(dialog,
                        null, getString(R.string.error_network_connection),
                        getString(R.string.dialog_ok), positiveAction, null, null);
                break;
            case AppConstants.ERROR_GETTING_CONFIG:
                positiveAction = new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) { finish(); }
                };
                dialog = buildDialog(dialog,
                        null, getString(R.string.config_unavailable),
                        getString(R.string.dialog_ok), positiveAction, null, null);
                break;
            case AppConstants.UPGRADE_REQUIRED:
                positiveAction = new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        try {
                            openPlayStore();
                        } catch (ActivityNotFoundException e) {
                            finish();
                        }
                    }
                };
                negativeAction = new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) { finish(); }
                };
                dialog = buildDialog(dialog,
                        getString(R.string.upgrade_required_title), getString(R.string.upgrade_required),
                        getString(R.string.dialog_play_store), positiveAction,
                        getString(R.string.dialog_close), negativeAction);
                break;
            case AppConstants.UPGRADE_RECOMMENDED:
                positiveAction = new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        try {
                            openPlayStore();
                        } catch (ActivityNotFoundException e) {
                            finish();
                        }
                    }
                };
                negativeAction = new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        getAccountFeed();
                    }
                };
                dialog = buildDialog(dialog,
                        getString(R.string.upgrade_recommended_title), getString(R.string.upgrade_recommended),
                        getString(R.string.dialog_play_store), positiveAction,
                        getString(R.string.dialog_later), negativeAction);
                break;
            default:
                break;
        }

        dialog.build()
                .show();
    }

    private void openPlayStore() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id="
                + getApplicationContext().getPackageName()));
        startActivity(intent);
    }

    private MaterialDialog.Builder buildDialog(MaterialDialog.Builder dialog,
                                       String title, String message,
                                       String positiveTitle, MaterialDialog.ButtonCallback positiveAction,
                                       String negativeTitle, MaterialDialog.ButtonCallback negativeAction) {
        if (title != null) {
            dialog.title(title);
        }
        if (message != null) {
            dialog.content(message);
        }
        if (positiveTitle != null && positiveAction != null) {
            dialog.positiveText(positiveTitle)
                    .callback(positiveAction);
        }
        if (negativeTitle != null && negativeAction != null) {
            dialog.negativeText(negativeTitle)
                    .callback(negativeAction);
        }
        return dialog;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
    }

}
