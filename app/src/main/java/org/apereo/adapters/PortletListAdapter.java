package org.apereo.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apereo.R;
import org.apereo.models.Portlet;
import org.apereo.utils.ImageManager;

import java.util.List;

/**
 * Created by schneis on 8/20/14.
 */
public class PortletListAdapter extends ArrayAdapter<Portlet> {

    Context context;
    int layoutResourceId;
    List<Portlet> data = null;
    int size;

    public PortletListAdapter(Context context, int layoutResourceId, List<Portlet> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PortletHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new PortletHolder();
            holder.portletIcon = (ImageView) row.findViewById(R.id.portletIcon);
            holder.txtName = (TextView) row.findViewById(R.id.name);
            holder.txtDescription = (TextView) row.findViewById(R.id.description);

            row.setTag(holder);
        } else {
            holder = (PortletHolder) row.getTag();
        }

        Portlet portlet = data.get(position);
        holder.txtName.setText(portlet.getName());
        holder.txtDescription.setText(portlet.getDescription());

        ImageManager.setImageFromUrl(holder.portletIcon, portlet.getIconUrl());
        return row;
    }

    static class PortletHolder {
        ImageView portletIcon;
        TextView txtName;
        TextView txtDescription;
    }
}
