package org.umobile.models;

import java.util.List;

/**
 * Created by schneis on 8/21/14.
 */
public class Layout {

    private List<Folder> folders;

    public static class Builder {
        private List<Folder> folders;


        public Builder setFolders(List<Folder> folders) {
            this.folders = folders;
            return this;
        }

        public Layout build() {
            return new Layout(this);
        }
    }

    private Layout(Builder builder) {
        folders = builder.folders;
    }

    public List<Folder> getFolders() {
        return folders;
    }

}
