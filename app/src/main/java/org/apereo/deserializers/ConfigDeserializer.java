package org.apereo.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.apereo.models.Config;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ConfigDeserializer implements JsonDeserializer<Config> {
    private final String TAG = ConfigDeserializer.class.getName();

    @Override
    public Config deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        JsonObject jObject = (JsonObject) jsonElement;

        boolean upgradeRecommended = jObject.getAsJsonPrimitive("upgradeRecommended").getAsBoolean();
        boolean upgradeRequired = jObject.getAsJsonPrimitive("upgradeRequired").getAsBoolean();

        JsonArray disabledPortletsJObject = jObject.getAsJsonArray("disabledPortlets");
        ArrayList<String> disabledPortlets = new ArrayList<String>();

        for (JsonElement entry : disabledPortletsJObject) {
            disabledPortlets.add(entry.getAsString());
        }

        Config config = new Config.Builder()
                .setUpgradeRecommended(upgradeRecommended)
                .setUpgradeRequired(upgradeRequired)
                .setDisabledPortlets(disabledPortlets)
                .build();

        return config;
    }
}

