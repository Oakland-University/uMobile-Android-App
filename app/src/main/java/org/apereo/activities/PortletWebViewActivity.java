package org.apereo.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.apache.commons.lang.StringUtils;
import org.apereo.App;
import org.apereo.R;
import org.apereo.adapters.FolderListAdapter;
import org.apereo.models.Folder;
import org.apereo.services.UmobileRestCallback;
import org.apereo.utils.CasClient;
import org.apereo.utils.LayoutManager;

import java.util.List;

/**
 * Created by schneis on 8/26/14.
 */
@EActivity(R.layout.activity_portlet_webview)
public class PortletWebViewActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private static final String TAG = PortletWebViewActivity.class.getName();
    private ActionBarDrawerToggle mDrawerToggle;
    private final String ACCOUNT_TYPE = App.getInstance().getResources().getString(R.string.account_type);

    @ViewById(R.id.webView)
    WebView webView;

    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @ViewById(R.id.left_drawer)
    ListView mDrawerList;

    @ViewById(R.id.progress_bar)
    ProgressBar progressBar;

    @Bean
    LayoutManager layoutManager;

    @Bean
    CasClient casClient;

    DownloadManager downloadManager;

    @Extra
    String url;

    @Extra
    String portletName;

    @Extra
    int folderPosition;

    @AfterViews
    void initialize() {
        setUpToolbar();
        setUpNavigationDrawer();
        setUpWebView();
        setUpProgressBar();
        setUpDownloadManager();

        // TODO find better way to do this
        url = url.replaceAll("/f/welcome","");
        webView.loadUrl(url);
    }

    private void setUpDownloadManager() {
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                // This is a general solution that is only confirmed working for Moodle.
                String domain = url.substring(0, StringUtils.ordinalIndexOf(url, "/", 3));
                String cookie = CookieManager.getInstance().getCookie(domain);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                        .addRequestHeader("Cookie", cookie)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                downloadManager.enqueue(request);
            }
        });
    }

    private void setUpWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setUserAgentString("Android.uMobile");
        webSettings.setJavaScriptEnabled(true);
        setUpAutomaticReauthentication(this);
    }

    private void setUpProgressBar() {
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && progressBar.getVisibility() == ProgressBar.GONE) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(ProgressBar.GONE);
                }
            }
        });
        progressBar.setY(mDrawerLayout.getTop());
    }

    private void setUpAutomaticReauthentication(final Activity activity) {
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, final String url) {
                if (url.startsWith(getResources().getString(R.string.login_url))) {
                    ActionClickListener listener = new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                            AccountManager accountManager = AccountManager.get(App.getInstance());
                            if (accountManager.getAccountsByType(ACCOUNT_TYPE).length != 0) {
                                Account account = accountManager.getAccountsByType(ACCOUNT_TYPE)[0];
                                casClient.authenticate(account.name, accountManager.getPassword(account), activity, new UmobileRestCallback<String>() {
                                    @Override
                                    public void onError(Exception e, String response) {
                                        showSnackBar(getString(R.string.error) + " " + getString(R.string.lockout_reminder));
                                    }
                                    @Override
                                    public void onSuccess(String response) {
                                        PortletWebViewActivity_.intent(activity)
                                                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                .url(url)
                                                .portletName(portletName)
                                                .start();
                                    }
                                });
                            } else {
                                LoginActivity_
                                        .intent(activity)
                                        .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        .url(url)
                                        .portletName(portletName)
                                        .start();
                            }
                        }
                    };
                    showSnackBarWithAction(getString(R.string.reauthenticating), listener, getString(R.string.login));
                }
            }
        });

    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(portletName);
    }

    private void setUpNavigationDrawer() {

        // Workaround for layoutManager/mDrawerList being possibly garbage collected
        try {
            List<Folder> folders = layoutManager.getLayout().getFolders();
            mDrawerList.setAdapter(new FolderListAdapter(this,
                    R.layout.drawer_list_item, folders, folderPosition));
        } catch (NullPointerException e) {
            restartApp();
        }

        mDrawerList.setOnItemClickListener(this);
        mDrawerList.setItemChecked(folderPosition, true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                toolbar,               /* pass in toolbar reference */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void restartApp() {
        LaunchActivity_
                .intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_page, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (App.getIsAuth()) {
            menu.findItem(R.id.login_action_bar_button).setVisible(false);
        } else {
            menu.findItem(R.id.logout_action_bar_button).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login_action_bar_button:
                logIn(getResources().getString(R.string.login_url));
                break;
            case R.id.logout_action_bar_button:
                logOut();
                break;
        }

        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }

    private void logIn(String url) {
        LoginActivity_
                .intent(this)
                .url(url)
                .start();
    }

    private void logOut() {
        HomePageActivity_
                .intent(PortletWebViewActivity.this)
                .shouldLogOut(true)
                .start();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((FolderListAdapter) mDrawerList.getAdapter()).setSelectedIndex(position);
        mDrawerList.setItemChecked(position, true);
        selectItem(position);
    }

    private void selectItem(int position) {
        HomePageActivity_
                .intent(this)
                .ePosition(position)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }

}
