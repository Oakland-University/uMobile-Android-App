package org.umobile.utils;

import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.umobile.models.Layout;

/**
 * Created by schneis on 8/21/14.
 */
@EBean(scope = EBean.Scope.Singleton)
public class LayoutManager {

    private Layout layout;

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public Layout getLayout() {
        return this.layout;
    }
}
