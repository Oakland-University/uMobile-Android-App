package org.apereo.models;

import org.apereo.App;

/**
 * Created by schneis on 8/20/14.
 */
public class Portlet {


    String name;
    String description;
    String url;
    String iconUrl;
    int resID;

    public static class Builder {
        private String name;
        private String description;
        private String url;
        private String iconUrl;
        private int resID;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }
        public Builder setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public Portlet build() {
            return new Portlet(this);
        }

        public Builder setDrawable(String drawable) {
            resID = App.getInstance().getResources().getIdentifier(drawable , "drawable", App.getInstance().getPackageName());
            return this;
        }
    }

    private Portlet(Builder builder) {
        name = builder.name;
        description = builder.description;
        url = builder.url;
        iconUrl = builder.iconUrl;
        resID = builder.resID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }
    public String getIconUrl() {
        return iconUrl;
    }
    public int getResID() {
        return resID;
    }
}
