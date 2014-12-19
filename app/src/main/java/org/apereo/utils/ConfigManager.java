package org.apereo.utils;

import org.androidannotations.annotations.EBean;
import org.apereo.models.Config;

@EBean(scope = EBean.Scope.Singleton)
public class ConfigManager {

    private Config config;

    public void setConfig(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return this.config;
    }
}
