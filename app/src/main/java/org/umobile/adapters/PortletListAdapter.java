package org.umobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.umobile.R;
import org.umobile.models.Portlet;

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
            holder.txtName = (TextView) row.findViewById(R.id.name);
            holder.txtDescription = (TextView) row.findViewById(R.id.description);

            row.setTag(holder);
        } else {
            holder = (PortletHolder) row.getTag();
        }

        Portlet portlet = data.get(position);
        holder.txtName.setText(portlet.getName());
        holder.txtDescription.setText(portlet.getDescription());

        return row;
    }

    static class PortletHolder {
        TextView txtName;
        TextView txtDescription;
    }
}
