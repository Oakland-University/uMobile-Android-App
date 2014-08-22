package org.umobile.deserializers;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.umobile.models.Folder;
import org.umobile.models.Layout;
import org.umobile.models.Portlet;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LayoutDeserializer implements JsonDeserializer<Layout> {

    @Override
    public Layout deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        Layout layout;
        JsonObject jObject = (JsonObject) jsonElement;

        List<Folder> folders = new ArrayList<Folder>();
        JsonObject layoutObject = jObject.getAsJsonObject("layout");
        if (layoutObject.has("folders") && layoutObject.get("folders").isJsonArray()) {
            JsonArray folderArray = layoutObject.getAsJsonArray("folders");
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
                                .setUrl(portlet.get("url").getAsString())
                                .build();
                        portlets.add(p);
                        Log.d("portlet name = " , p.getName());
                    }
                }
                Folder f = new Folder.Builder()
                        .setName(folder.get("title").getAsString())
                        .setPortlets(portlets)
                        .build();

                folders.add(f);
            }
        }
        layout = new Layout.Builder()
                .setFolders(folders)
                .build();

        return layout;
    }
}

