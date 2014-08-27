package org.apereo.models;

/**
 * Created by schneis on 8/20/14.
 */
public class Portlet {


    String name;
    String description;
    String url;

    public static class Builder {
        private String name;
        private String description;
        private String url;



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

        public Portlet build() {
            return new Portlet(this);
        }
    }

    private Portlet(Builder builder) {
        name = builder.name;
        description = builder.description;
        url = builder.url;
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
}
