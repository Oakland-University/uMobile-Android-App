package org.umobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.umobile.R;
import org.umobile.models.Folder;

/**
 * Created by schneis on 8/20/14.
 */
public class FolderListAdapter extends ArrayAdapter<Folder> {

    Context context;
    int layoutResourceId;
    Folder data[] = null;
    int size;

    public FolderListAdapter(Context context, int layoutResourceId, Folder[] data) {
        super(context, layoutResourceId, data);
        Log.d("HELLOOOOO", "" + data.length);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        FolderHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new FolderHolder();
            holder.txtName = (TextView) row.findViewById(R.id.name);

            row.setTag(holder);
        } else {
            holder = (FolderHolder) row.getTag();
        }

        Folder folder = data[position];
        holder.txtName.setText(folder.getName());

        return row;
    }

    static class FolderHolder {
        TextView txtName;
    }
}
