package org.apereo.activities;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.apereo.R;
import org.apereo.utils.Logger;

/**
 * Created by schneis on 8/26/14.
 */
@EActivity(R.layout.portlet_webview)
public class PortletWebViewActivity extends BaseActivity{

    private static final String TAG = PortletWebViewActivity.class.getName();
    @ViewById(R.id.webview)
    WebView webView;

    @Extra
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void initiailize() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        //TODO find better way to do this
        url = url.replaceAll("/f/welcome","");

        Logger.d(TAG, url);
        webView.loadUrl(url);
    }
}
