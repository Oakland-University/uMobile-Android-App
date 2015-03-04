package org.apereo.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.apache.commons.lang.StringUtils;
import org.apereo.App;
import org.apereo.R;
import org.apereo.activities.LaunchActivity_;
import org.apereo.adapters.PortletListAdapter;
import org.apereo.constants.AppConstants;
import org.apereo.interfaces.IActionListener;
import org.apereo.models.Portlet;
import org.apereo.utils.LayoutManager;

import java.util.List;

@EFragment(R.layout.activity_listview)
public class HomePageListFragment extends ListFragment {
    private Bundle mArguments;
    private View view;
    private PortletListAdapter adapter;
    private List<Portlet> portlets = null;
    private final String TAG = HomePageListFragment.class.getName();
    private Activity activity;
    private int position;
    private float parallaxRate = 1.9f;

    @org.androidannotations.annotations.res.ColorRes(R.color.theme_accent)
    int themeAccent;

    @org.androidannotations.annotations.res.ColorRes(R.color.theme_accent_dark)
    int themeAccentDark;

    @org.androidannotations.annotations.res.ColorRes(R.color.theme_light)
    int themeLight;

    @org.androidannotations.annotations.res.ColorRes(R.color.theme_dark)
    int themeDark;

    ListView list;
    View header;

    @Bean
    LayoutManager layoutManager;

    private IActionListener actionListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mArguments != null){
            position = mArguments.getInt(AppConstants.POSITION);
        }
        return getView();
    }

    @AfterViews
    void initialize() {
        list = getListView();
        header = getActivity().getLayoutInflater().inflate(R.layout.header, null);
        list.addHeaderView(header, null, false);
        fetchLayout();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = parent.getItemAtPosition(position);
                if (o instanceof Portlet) {
                    Portlet p = (Portlet) o;
                    // TODO have a flag in portlet to decide this
                    boolean concat = !StringUtils.equalsIgnoreCase(p.getIconUrl(), getResources().getString(R.string.use_drawable));
                    actionListener.launchWebView(p.getName(), concat ? App.getRootUrl().concat(p.getUrl()) : p.getUrl());
                }
            }
        });
        list.setOnScrollListener(onScrollListener);
    }

    private void fetchLayout() {
        try {
            portlets = layoutManager.getLayout().getFolders().get(position).getPortlets();
            adapter = new PortletListAdapter(activity, R.layout.portlet_row, portlets);
            setListAdapter(adapter);
        } catch (NullPointerException e) {
            LaunchActivity_.intent(App.getInstance());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) { }

        @Override
        public void onScroll(AbsListView view, final int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            try {
                if (firstVisibleItem == 0) {
                    int childTop = list.getChildAt(1).getTop();
                    int headerHeight = header.getHeight();
                    parallaxHeader(childTop, headerHeight);

                    float ratio = (float) childTop / headerHeight;
                    if (ratio > 1) {
                        ratio = 1;
                    } else if (ratio < 0) {
                        ratio = 0;
                    }

                    updateToolbarColor(ratio);
                    if (Build.VERSION.SDK_INT >= 21) {
                        updateStatusBarColor(ratio);
                    }
                } else {
                    ((ActionBarActivity) getActivity()).getSupportActionBar()
                            .setBackgroundDrawable(new ColorDrawable(themeAccent));
                    if (Build.VERSION.SDK_INT >= 21) {
                        getActivity().getWindow().setStatusBarColor(themeAccentDark);
                    }
                }
            } catch (NullPointerException e) { }
        }
    };

    private void updateToolbarColor(float ratio) {

        int mainRed = Color.red(themeLight);
        int mainBlue = Color.blue(themeLight);
        int mainGreen = Color.green(themeLight);

        int accentRed = Color.red(themeAccent);
        int accentGreen = Color.green(themeAccent);
        int accentBlue = Color.blue(themeAccent);

        int newRed =  (int) ((mainRed * ratio) + (accentRed * (1 - ratio)));
        int newGreen = (int) ((mainGreen * ratio) + (accentGreen * (1 - ratio)));
        int newBlue = (int) ((mainBlue * ratio) + (accentBlue * (1 - ratio)));

        int newColor = Color.rgb(newRed, newGreen, newBlue);

        ((ActionBarActivity) getActivity()).getSupportActionBar()
                .setBackgroundDrawable(new ColorDrawable(newColor));

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateStatusBarColor(float ratio) {

        int mainRed = Color.red(themeDark);
        int mainGreen = Color.green(themeDark);
        int mainBlue = Color.blue(themeDark);

        int accentRed = Color.red(themeAccentDark);
        int accentGreen = Color.green(themeAccentDark);
        int accentBlue = Color.blue(themeAccentDark);

        int newRed =  (int) ((mainRed * ratio) + (accentRed * (1 - ratio)));
        int newGreen = (int) ((mainGreen * ratio) + (accentGreen * (1 - ratio)));
        int newBlue = (int) ((mainBlue * ratio) + (accentBlue * (1 - ratio)));

        int newColor = Color.rgb(newRed, newGreen, newBlue);

        getActivity().getWindow().setStatusBarColor(newColor);
    }

    private void parallaxHeader(int childTop, int headerHeight) {
        float heightToMatch = (headerHeight - childTop) / parallaxRate;
        header.setY(-heightToMatch);
        header.setAlpha(1 - (heightToMatch / headerHeight));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mArguments = getArguments();
        this.activity = activity;
    }

    public void update() {
        try {
            portlets = layoutManager.getLayout().getFolders().get(position).getPortlets();
            adapter.notifyDataSetChanged();
        } catch (NullPointerException e) { }
    }

    public static Fragment getFragment(IActionListener actionListener) {
        HomePageListFragment fragment = new HomePageListFragment_();
        fragment.setActionListener(actionListener);
        return fragment;
    }

    public void setActionListener(IActionListener actionListener) {
        this.actionListener = actionListener;
    }
}

