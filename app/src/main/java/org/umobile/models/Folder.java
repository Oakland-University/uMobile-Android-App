package org.umobile.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schneis on 8/20/14.
 */
public class Folder {


    private String name;
    List<Portlet> portlets;

    public static class Builder {
        private String name;
        private List<Portlet> portlets;


        public Builder setPortlets(List<Portlet> portlets) {
            this.portlets = portlets;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Folder build() {
            return new Folder(this);
        }
    }

    private Folder(Builder builder) {
        portlets = builder.portlets;
        name = builder.name;
    }

    public String getName() {
        return name;
    }

    public List<Portlet> getPortlets() {
        return portlets;
    }
}
