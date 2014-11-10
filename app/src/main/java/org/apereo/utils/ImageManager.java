package org.apereo.utils;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.apereo.App;
import org.apereo.R;

/**
 * Created by schneis on 8/26/14.
 */
public class ImageManager {

    public static void setImageFromUrl(ImageView imageView, String path) {
        try {
            Picasso.with(imageView.getContext())
                    .load(App.getRootUrl().concat(path))
                    .error(R.drawable.ic_launcher)
                    .into(imageView);
        } catch (Exception e) {
            Logger.e("image", e.getMessage(), e);
        }

    }

    public static void setImageFromDrawable(ImageView imageView, int resNo) {
        try {
            Picasso.with(imageView.getContext())
                    .load(resNo)
                    .error(R.drawable.ic_launcher)
                    .into(imageView);
        } catch (Exception e) {
        }

    }
}
