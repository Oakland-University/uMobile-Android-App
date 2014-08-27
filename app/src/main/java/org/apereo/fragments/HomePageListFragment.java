package org.apereo.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.apereo.App;
import org.apereo.activities.HomePage;
import org.apereo.activities.PortletWebViewActivity;
import org.apereo.activities.PortletWebViewActivity_;
import org.apereo.adapters.PortletListAdapter;
import org.apereo.models.Portlet;
import org.apereo.utils.LayoutManager;
import java.util.List;

import org.apereo.R;

@EFragment(R.layout.activity_listview)
public class HomePageListFragment extends ListFragment {
    private FadingActionBarHelper mFadingHelper;
    private Bundle mArguments;
    private View view;
    private PortletListAdapter adapter;
    private List<Portlet> portlets = null;
    private final String TAG = HomePageListFragment.class.getName();
    private Activity activity;

    @Bean
    LayoutManager layoutManager;

    public static final String ARG_IMAGE_RES = "image_source";
    public static final String ARG_ACTION_BG_RES = "image_action_bs_res";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = mFadingHelper.createView(inflater);
        Log.d(TAG, "onCreateView");

        if (mArguments != null){
            ImageView img = (ImageView) view.findViewById(R.id.image_header);
            img.setImageResource(mArguments.getInt(ARG_IMAGE_RES));
        }

        return view;
    }
    @AfterViews
    void initialize() {
        Log.d("layout folder size = ", "" + layoutManager.getLayout().getFolders().size());
        portlets = layoutManager.getLayout().getFolders().get(0).getPortlets();
        Log.d("PORTLETS SIZE = ", ""+portlets.size());
        adapter = new PortletListAdapter(activity, R.layout.portlet_row, portlets);
        setListAdapter(adapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = parent.getItemAtPosition(position);
                if (o instanceof Portlet) {
                    Portlet p = (Portlet) o;
                    ((HomePage)getActivity()).lauchWebView(App.getRootUrl().concat(p.getUrl()));
                }
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        int actionBarBg = R.drawable.ab_background_light;
        mFadingHelper = new FadingActionBarHelper()
                .actionBarBackground(actionBarBg)
                .headerLayout(R.layout.header)
                .contentLayout(R.layout.activity_listview);
        mFadingHelper.initActionBar(activity);

        Log.d(TAG, "onAttach");
        this.activity = activity;


    }
    public void update(int resourseId) {
        portlets = layoutManager.getLayout().getFolders().get(0).getPortlets();
        adapter.notifyDataSetChanged();
    }

}

