package org.apereo.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.apereo.App;
import org.apereo.R;
import org.apereo.deserializers.LayoutDeserializer;
import org.apereo.models.Folder;
import org.apereo.models.Layout;
import org.apereo.models.Portlet;
import org.apereo.services.RestApi;
import org.apereo.services.UmobileRestCallback;
import org.apereo.utils.CasClient;
import org.apereo.utils.ConfigManager;
import org.apereo.utils.LayoutManager;
import org.apereo.utils.Logger;

/**
 * Created by schneis on 8/28/14.
 */
@EActivity(R.layout.activity_login_page)
public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getName();

    private final String ACCOUNT_TYPE = App.getInstance().getResources().getString(R.string.account_type);

    @ViewById(R.id.login_container)
    RelativeLayout container;

    @ViewById(R.id.web_view)
    WebView webView;

    @ViewById(R.id.login_username)
    EditText userNameView;
    @ViewById(R.id.login_password)
    EditText passwordView;
    @ViewById(R.id.rememberMe)
    CheckBox rememberMe;
    @ViewById(R.id.forgot_password)
    TextView forgotPassword;

    @Extra
    String username;
    @Extra
    String password;
    @Extra
    String url;

    @Bean
    RestApi restApi;

    @Bean
    CasClient casClient;

    @Bean
    LayoutManager layoutManager;

    @Bean
    ConfigManager configManager;

    AccountManager accountManager =
            (AccountManager) App.getInstance().getSystemService(ACCOUNT_SERVICE);

    @AfterViews
    void initialize() {
        passwordView.setTypeface(Typeface.DEFAULT);
        passwordView.setTransformationMethod(new PasswordTransformationMethod());

        passwordView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    loginClick();
                    return true;
                }
                return false;
            }
        });

        checkAccount(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAccount(boolean initialCheck) {
        if (username != null) {
            Account newAccount = new Account(username, ACCOUNT_TYPE);

            accountManager.addAccountExplicitly(newAccount, password, null);

            if (initialCheck) {
                container.setVisibility(View.GONE);
                logIn();
            }
        }
    }


    @Click(R.id.login_button)
    protected void loginClick() {
        if (!userNameView.getText().toString().isEmpty() &&
                !passwordView.getText().toString().isEmpty()) {
            username = userNameView.getText().toString();
            password = passwordView.getText().toString();

            logIn();
        } else {
            showShortToast(getResources().getString(R.string.form_error));
        }
    }


    @Click(R.id.forgot_password)
    protected void forgotPasswordClick() {
        PortletWebViewActivity_
                .intent(LoginActivity.this)
                .url(App.getInstance().getResources().getString(R.string.forgot_password_url))
                .start();
    }

    protected void logIn() {
        showSpinner("Logging in...");

        casClient.authenticate(username, password, this, new UmobileRestCallback<String>() {
            @Override
            public void onSuccess(String response) {
                getFeed();
            }

            @Override
            public void onError(final Exception e, final String responseBody) {
                dismissSpinner();
                if (responseBody != null && !responseBody.isEmpty()) {
                    showLongToast(responseBody);
                } else {
                    showLongToast(getString(R.string.error_logging_in));
                }

                deleteAccount();
                restartActivity();
            }
        });
    }

    private void deleteAccount() {
        if (accountManager.getAccountsByType(ACCOUNT_TYPE).length != 0) {
            casClient.logOut(new UmobileRestCallback<Integer>() {
                @Override
                public void onError(Exception e, Integer response) {
                    Logger.e(TAG, "error logging out (received status code " + response + ")", e);
                    dismissSpinner();
                    showLongToast(getString(R.string.error_logging_out));
                }
                @Override
                public void onSuccess(Integer response) { }
            });
        }
    }

    private void restartActivity() {
        finish();
        LoginActivity_.intent(this).start();
    }

    private void getFeed() {
        restApi.getMainFeed(this, new UmobileRestCallback<String>() {

            @Override
            public void onError(Exception e, String responseBody) {
                Logger.e(TAG, responseBody, e);
            }

            @Override
            public void onSuccess(String response) {
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

                if (rememberMe.isChecked()) {
                    checkAccount(false);
                }

                HomePageActivity_
                        .intent(LoginActivity.this)
                        .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .start();
                finish();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                dismissSpinner();
            }
        });
    }

}
