package org.umobile.models;

import java.util.List;

/**
 * Created by schneis on 8/20/14.
 */
public class Folder {


    private String name;
    List<Portlet> portlets;

    public Folder(){}
    public Folder(String name, List<Portlet> portlets) {
        this.name = name;
        this.portlets = portlets;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Portlet> getPortlets() {
        return portlets;
    }

    public void setPortlets(List<Portlet> portlets) {
        this.portlets = portlets;
    }
}
