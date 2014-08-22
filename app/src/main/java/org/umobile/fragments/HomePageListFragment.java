package org.umobile.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.umobile.R;

public class HomePageListFragment extends ListFragment {
    private FadingActionBarHelper mFadingHelper;
    private Bundle mArguments;
    private View view;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> portlets = null;
    private final String TAG = HomePageListFragment.class.getName();

    public static final String ARG_IMAGE_RES = "image_source";
    public static final String ARG_ACTION_BG_RES = "image_action_bs_res";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = mFadingHelper.createView(inflater);

        if (mArguments != null){
            ImageView img = (ImageView) view.findViewById(R.id.image_header);
            img.setImageResource(mArguments.getInt(ARG_IMAGE_RES));
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mArguments = getArguments();
        int actionBarBg = R.drawable.ab_background_light;
        int resourceId = mArguments.getInt("JSON");

        mFadingHelper = new FadingActionBarHelper()
                .actionBarBackground(actionBarBg)
                .headerLayout(R.layout.header)
                .contentLayout(R.layout.activity_listview);
        mFadingHelper.initActionBar(activity);

        try {
            portlets = loadItems(resourceId);
        }catch (IOException ie) {
            Log.e(TAG,ie.getMessage(), ie);
        }catch (XmlPullParserException xppe) {
            Log.e(TAG, xppe.getMessage(), xppe);
        }

        Log.d("PORTLETS SIZE = ", ""+portlets.size());
        adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, portlets);
        setListAdapter(adapter);
    }
    /**
     * @return A list of Strings read from the specified resource
     */
    private ArrayList<String> loadItems(int rawResourceId) throws XmlPullParserException, IOException {

        ArrayList<String> portletsList = new ArrayList<String>();

        InputStream is = getResources().openRawResource(rawResourceId);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        Log.d(TAG, "in load items");

        String json = writer.toString();
        Log.d(TAG, "json = "+json);


        try {
            JSONObject jObject = new JSONObject(json);

            JSONArray jArray = jObject.getJSONArray("portlets");
            for (int i=0; i < jArray.length(); i++)
            {
                JSONObject oneObject = jArray.getJSONObject(i);
                // Pulling items from the array
                String name = oneObject.getString("name");
                String description = oneObject.getString("description");
                String url = oneObject.getString("url");
                Log.d(TAG, "in loop");

                portletsList.add(name);
                Log.d("PORTLETLISTSIZE = " , ""+portletsList.size());
            }

        }catch (JSONException je) {
            Log.e(TAG, je.getMessage(), je);
        }

        return portletsList;

    }

    public void update(int resourseId) {
        try {
            portlets = loadItems(resourseId);
        }catch (IOException ie) {
            Log.e(TAG,ie.getMessage(), ie);
        }catch (XmlPullParserException xppe) {
            Log.e(TAG, xppe.getMessage(), xppe);
        }
        adapter.notifyDataSetChanged();
    }

}

