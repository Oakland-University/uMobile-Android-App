package org.apereo.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
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
import org.apache.commons.lang.StringUtils;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schneis on 8/28/14.
 */
@EActivity(R.layout.activity_login_page)
public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getName();
    private final String ACCOUNT_TYPE = App.getInstance().getResources().getString(R.string.account_type);
    private boolean usingConfig = false;

    @ViewById(R.id.login_container)
    RelativeLayout container;
    @ViewById(R.id.login_username)
    EditText userNameView;
    @ViewById(R.id.login_password)
    EditText passwordView;
    @ViewById(R.id.rememberMe)
    CheckBox rememberMe;
    @ViewById(R.id.forgot_password)
    TextView forgotPassword;
    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @Extra
    String username;
    @Extra
    String password;
    @Extra
    String url;
    @Extra
    String portletName;

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
        setUpToolbar();
        setUpPasswordView();

        usingConfig = getResources().getBoolean(R.bool.shouldUseGlobalConfig);

        checkAccount(true);
    }

    private void setUpPasswordView() {
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
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                logInWithConfig();
            }
        }
    }

    @Click(R.id.login_button)
    protected void loginClick() {
        if (!userNameView.getText().toString().isEmpty() &&
                !passwordView.getText().toString().isEmpty()) {
            username = userNameView.getText().toString().toLowerCase();
            password = passwordView.getText().toString();
            logInWithConfig();
        } else {
            showSnackBar(this, getResources().getString(R.string.form_error));
        }
    }

    @Click(R.id.forgot_password)
    protected void forgotPasswordClick() {
        PortletWebViewActivity_
                .intent(LoginActivity.this)
                .url(App.getInstance().getResources().getString(R.string.forgot_password_url))
                .start();
    }

    protected void logInWithConfig() {
        showSpinner("Logging in...");

        if (usingConfig) {
            restApi.getGlobalConfig(this, new UmobileRestCallback<String>() {
                @Override
                public void onError(Exception e, String response) { }

                @Override
                public void onSuccess(String response) {
                    logInToCas();
                }
            });
        } else {
            logInToCas();
        }
    }

    private void logInToCas() {
        casClient.authenticate(username, password, this, new UmobileRestCallback<String>() {
            @Override
            public void onSuccess(String response) { getFeed(); }

            @Override
            public void onError(final Exception e, final String responseBody) {
                dismissSpinner();
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loginClick();
                    }
                };
                showSnackBarWithAction(LoginActivity.this, responseBody + " " + getString(R.string.lockout_reminder), listener, getString(R.string.retry));
                deleteAccount();
            }
        });
    }

    private void deleteAccount() {
        if (accountManager.getAccountsByType(ACCOUNT_TYPE).length != 0) {
            casClient.logOut(new UmobileRestCallback<Integer>() {
                @Override
                public void onError(Exception e, Integer response) {
                    dismissSpinner();
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteAccount();
                        }
                    };
                    showSnackBarWithAction(LoginActivity.this, getString(R.string.error), listener, getString(R.string.retry));
                }
                @Override
                public void onSuccess(Integer response) { }
            });
        }
    }

    private void getFeed() {
        restApi.getMainFeed(this, new UmobileRestCallback<String>() {

            @Override
            public void onError(Exception e, String responseBody) { }

            @Override
            public void onSuccess(String response) {
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

                if (rememberMe.isChecked()) {
                    checkAccount(false);
                }

                App.setIsAuth(true);

                if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(portletName)) {
                    PortletWebViewActivity_
                            .intent(LoginActivity.this)
                            .url(url)
                            .portletName(portletName)
                            .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .start();
                } else {
                    HomePageActivity_
                            .intent(LoginActivity.this)
                            .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .start();
                }

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
