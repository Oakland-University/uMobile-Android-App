package org.apereo.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.apereo.App;
import org.apereo.R;
import org.apereo.models.Folder;
import org.apereo.models.Layout;
import org.apereo.models.Portlet;
import org.apereo.utils.Logger;
import org.apereo.utils.RawJSONLoader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LayoutDeserializer implements JsonDeserializer<Layout> {
    private List<Folder> folders = new ArrayList<Folder>();
    private final String TAG = LayoutDeserializer.class.getName();

    @Override
    public Layout deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        Layout layout;
        JsonObject jObject = (JsonObject) jsonElement;

        // build the folder and portlet objects from
        // layout.json feed
        JsonObject layoutObject = jObject.getAsJsonObject("layout");
        if (layoutObject.has("folders") && layoutObject.get("folders").isJsonArray()) {
            JsonArray folderArray = layoutObject.getAsJsonArray("folders");
            fillFolders(folderArray);
        }

        // add the other services portlets from a raw json feed
        // saved in R.raw
        try {
            String otherPortlets = RawJSONLoader.loadFeed(R.raw.otherportlets);
            JsonParser jsonParser = new JsonParser();
            JsonObject otherObject = (JsonObject)jsonParser.parse(otherPortlets);
            JsonArray otherFolderArray = otherObject.getAsJsonArray("folders");
            fillFolders(otherFolderArray);
        }catch (IOException ioe) {
            Logger.e(TAG, ioe.getMessage(), ioe);
        }

        // build the final layout from both feeds
        layout = new Layout.Builder()
                .setFolders(folders)
                .build();

        return layout;
    }

    /**
     * Build the folder and portlet objects
     * @param folderArray
     */
    public void fillFolders(JsonArray folderArray) {
        for (int i = 0; i < folderArray.size(); i++) {
            JsonObject folder = folderArray.get(i).getAsJsonObject();
            List<Portlet> portlets = new ArrayList<Portlet>();
            if (folder.has("portlets") && folder.get("portlets").isJsonArray()) {
                JsonArray portletArray = folder.getAsJsonArray("portlets");
                for (int j = 0; j < portletArray.size(); j++) {
                    JsonObject portlet = portletArray.get(j).getAsJsonObject();
                    Portlet p = new Portlet.Builder()
                            .setName(portlet.get("title").getAsString())
                            .setDescription(portlet.get("description").getAsString())
                            .setIconUrl(portlet.has("iconUrl") ? portlet.get("iconUrl").getAsString() :
                                App.getInstance().getResources().getString(R.string.use_drawable))
                            .setUrl(portlet.get("url").getAsString())
                            .setDrawable(portlet.has("drawable") ? portlet.get("drawable").getAsString() : "")
                            .build();
                    portlets.add(p);
                }

            }
            Folder f = new Folder.Builder()
                    .setName(folder.get("title").getAsString())
                    .setPortlets(portlets)
                    .build();

            folders.add(f);
        }
    }
}

