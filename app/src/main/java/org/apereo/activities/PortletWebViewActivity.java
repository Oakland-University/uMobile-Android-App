package org.apereo.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.apereo.App;
import org.apereo.R;
import org.apereo.adapters.FolderListAdapter;
import org.apereo.models.Folder;
import org.apereo.utils.LayoutManager;

import java.util.List;

/**
 * Created by schneis on 8/26/14.
 */
@EActivity(R.layout.activity_portlet_webview)
public class PortletWebViewActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private static final String TAG = PortletWebViewActivity.class.getName();
    private ActionBarDrawerToggle mDrawerToggle;

    @ViewById(R.id.webView)
    WebView webView;

    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @ViewById(R.id.left_drawer)
    ListView mDrawerList;

    @Bean
    LayoutManager layoutManager;

    @Extra
    String url;

    @Extra
    String portletName;

    @Extra
    int folderPosition;

    @AfterViews
    void initialize() {
        if (url.equals(getResources().getString(R.string.login_url))) {
            LaunchActivity_.intent(this);
        }

        setUpNavigationDrawer();

        showSpinner();

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        //TODO find better way to do this
        url = url.replaceAll("/f/welcome","");

        webView.loadUrl(url);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (url.equals(getResources().getString(R.string.login_url))) {
            LaunchActivity_.intent(this);
        }
    }

    private void setUpNavigationDrawer() {

        setSupportActionBar(toolbar);

        List<Folder> folders = null;
        // workaround for layoutManager being possibly garbage collected
        try {
            folders = layoutManager.getLayout().getFolders();
        } catch (NullPointerException e) {
            LaunchActivity_.intent(this);
        }

        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new FolderListAdapter(this,
                R.layout.drawer_list_item, folders, folderPosition));
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
        getSupportActionBar().setTitle(portletName);

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
