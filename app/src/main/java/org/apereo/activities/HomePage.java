package org.apereo.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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
import org.apereo.fragments.HomePageListFragment;
import org.apereo.interfaces.IActionListener;
import org.apereo.models.Folder;
import org.apereo.services.RestApi;
import org.apereo.utils.LayoutManager;

import java.util.List;

@EActivity(R.layout.activity_home_page)
public class HomePage extends BaseActivity implements IActionListener, AdapterView.OnItemClickListener {

    private final String TAG = HomePage.class.getName();

    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.left_drawer)
    ListView mDrawerList;

    @Bean
    RestApi restApi;

    @Bean
    LayoutManager layoutManager;

    @Extra
    int ePosition;

    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle = mDrawerTitle = getTitle();

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

    }

    @AfterViews
    void init() {

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        List<Folder> folders = layoutManager.getLayout().getFolders();
        mDrawerList.setAdapter(new FolderListAdapter(this,
                R.layout.drawer_list_item, folders, ePosition));
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
                getActionBar().setIcon(R.drawable.ic_launcher);
                getActionBar().setTitle(mTitle);
            }

            public void onDrawerOpened(View drawerView) {
            }
        };

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
        if(App.getIsAuth()) {
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
                login(getResources().getString(R.string.login_url));
                break;
            case R.id.logout_action_bar_button:
                login(getResources().getString(R.string.logout_url));
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

        ((FolderListAdapter) mDrawerList.getAdapter()).setSelectedIndex(position);
        ePosition = position;

        // update the main content by replacing fragments
        Bundle args = new Bundle();
        args.putInt(AppConstants.POSITION, position);

        Fragment fragment = HomePageListFragment.getFragment(this);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(layoutManager.getLayout().getFolders().get(position).getName());

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
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
