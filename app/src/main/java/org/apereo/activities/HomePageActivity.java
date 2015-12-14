package org.apereo.activities;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apereo.App;
import org.apereo.R;
import org.apereo.adapters.FolderListAdapter;
import org.apereo.constants.AppConstants;
import org.apereo.deserializers.LayoutDeserializer;
import org.apereo.fragments.HomePageListFragment;
import org.apereo.interfaces.IActionListener;
import org.apereo.models.Folder;
import org.apereo.models.Layout;
import org.apereo.services.RestApi;
import org.apereo.services.UmobileRestCallback;
import org.apereo.utils.CasClient;
import org.apereo.utils.LayoutManager;

import java.util.List;

@EActivity(R.layout.activity_home_page)
public class HomePageActivity extends BaseActivity implements IActionListener, AdapterView.OnItemClickListener {

    private final String TAG = HomePageActivity.class.getName();

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @ViewById(R.id.left_drawer)
    ListView mDrawerList;

    @Bean
    RestApi restApi;
    @Bean
    CasClient casClient;
    @Bean
    LayoutManager layoutManager;

    @Extra
    int ePosition;
    @Extra
    boolean shouldLogOut;

    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (shouldLogOut) {
            shouldLogOut = false;
            logOut();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle args = new Bundle();
        args.putInt(AppConstants.POSITION, ePosition);
        replaceFragment(args);
        invalidateOptionsMenu();
    }

    @AfterViews
    void init() {
        setUpToolbar();
        setUpNavigationDrawer();
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        mTitle = toolbar.getTitle();
    }

    private void setUpNavigationDrawer() {

        // workaround for layoutManager/mDrawerList being possibly garbage collected
        try {
            List<Folder> folders = layoutManager.getLayout().getFolders();
            mDrawerList.setAdapter(new FolderListAdapter(this,
                    R.layout.drawer_list_item, folders, ePosition));
        } catch (NullPointerException e) {
            restartApp();
        }

        mDrawerList.setOnItemClickListener(this);

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

        selectItem(ePosition);
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

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            super.onBackPressed();
        }
    }

    private void logIn(String url) {
        LoginActivity_
                .intent(this)
                .url(url)
                .start();
    }

    private void logOut() {
        showSpinner("Logging out...");

        casClient.logOut(new UmobileRestCallback<Integer>() {
            @Override
            public void onError(Exception e, Integer response) {
                dismissSpinner();
                showSnackBar(HomePageActivity.this, getString(R.string.error));
            }

            @Override
            public void onSuccess(Integer response) {
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.account_type), MODE_PRIVATE);
                sharedPreferences.edit().putBoolean("rememberMe", false).apply();
                getFeed();
            }
        });
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

        try {
            ((FolderListAdapter) mDrawerList.getAdapter()).setSelectedIndex(position);
        } catch (NullPointerException e) {
            restartApp();
        }

        ePosition = position;

        // update the main content by replacing fragments
        Bundle args = new Bundle();
        args.putInt(AppConstants.POSITION, position);
        replaceFragment(args);

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);

        // Workaround for layoutManager being possibly garbage collected
        try {
            setTitle(layoutManager.getLayout().getFolders().get(position).getName());
        } catch (NullPointerException e) {
            restartApp();
        }

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void replaceFragment(Bundle args) {
        Fragment fragment = HomePageListFragment.getFragment(this);
        fragment.setArguments(args);

        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    private void restartApp() {
        LaunchActivity_
                .intent(this)
                .start();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    private void getFeed() {
        restApi.getMainFeed(this, new UmobileRestCallback<String>() {
            @Override
            public void onError(Exception e, String responseBody) {
            }

            @Override
            public void onSuccess(String response) {
                Gson g = new GsonBuilder()
                        .registerTypeAdapter(Layout.class, new LayoutDeserializer())
                        .create();

                Layout layout = g.fromJson(response, Layout.class);
                layoutManager.setLayout(layout);
            }

            @Override
            public void onFinish() {
                reconfigureViews();
                dismissSpinner();
            }
        });
    }

    // Resets the activity's UI based on the current JSON layout, for example after logging out.
    @UiThread
    protected void reconfigureViews() {
        clearBackStack();

        // Update the portlet list.
        Bundle args = new Bundle();
        args.putInt(AppConstants.POSITION, 0);

        replaceFragment(args);

        // Update the login/logout button.
        invalidateOptionsMenu();

        // Workaround for layoutManager being possibly garbage collected
        try {
            // Update the side drawer contents.
            List<Folder> folders = layoutManager.getLayout().getFolders();
            mDrawerList.setAdapter(new FolderListAdapter(this,
                    R.layout.drawer_list_item, folders, ePosition));
        } catch (NullPointerException e) {
            restartApp();
        }

        // Switch back to the first tab.
        selectItem(0);

        dismissSpinner();
    }

    private void clearBackStack() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @UiThread
    @Override
    public void launchWebView(String portletName, String url) {
        PortletWebViewActivity_
                .intent(this)
                .url(url)
                .folderPosition(ePosition)
                .portletName(portletName)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .start();
    }
}
