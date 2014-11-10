package org.apereo.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.apereo.utils.Authenticator;

public class AuthenticatorService extends Service {

    private Authenticator mAuthenticator;
    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
