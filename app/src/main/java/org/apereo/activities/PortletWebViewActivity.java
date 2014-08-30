package org.apereo.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Adapter;
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
import org.apereo.utils.Logger;

import java.util.List;

/**
 * Created by schneis on 8/26/14.
 */
@EActivity(R.layout.portlet_webview)
public class PortletWebViewActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private static final String TAG = PortletWebViewActivity.class.getName();
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private static final int MENU_LOGIN = Menu.FIRST;
    private static final int MENU_LOGOUT = Menu.FIRST + 1;

    @ViewById(R.id.webView)
    WebView webView;

    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.left_drawer)
    ListView mDrawerList;

    @Bean
    LayoutManager layoutManager;

    @Extra
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle = mDrawerTitle = getTitle();

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @AfterViews
    void initiailize() {
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        List<Folder> folders = layoutManager.getLayout().getFolders();
        mDrawerList.setAdapter(new FolderListAdapter(this,
                R.layout.drawer_list_item, folders));
        mDrawerList.setOnItemClickListener(this);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setIcon(R.drawable.umobile_icon);
                getActionBar().setTitle(mTitle);
            }

            public void onDrawerOpened(View drawerView) {
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        //TODO find better way to do this
        url = url.replaceAll("/f/welcome","");

        Logger.d(TAG, url);
        webView.loadUrl(url);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if(App.getIsAuth()) {
            menu.add(0, MENU_LOGOUT, Menu.NONE, R.string.logout);
        }
        else {
            menu.add(0, MENU_LOGIN, Menu.NONE, R.string.login);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_LOGIN: login(getResources().getString(R.string.login_url)); break;
            case MENU_LOGOUT: login(getResources().getString(R.string.logout_url)); break;
        }

        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }

    private void login(String url) {
        LoginActivity_
                .intent(this)
                .url(url)
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
        selectItem(position);
    }

    private void selectItem(int position) {
        HomePage_
                .intent(this)
                .ePosition(position)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }

}
