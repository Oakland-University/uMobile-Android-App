package org.apereo.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.apereo.models.Config;

import java.lang.reflect.Type;

public class ConfigDeserializer implements JsonDeserializer<Config> {
    private boolean shouldUseConfig = false;
    private boolean upgradeRecommended;
    private boolean upgradeRequired;
    private final String TAG = ConfigDeserializer.class.getName();

    @Override
    public Config deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        JsonObject jObject = (JsonObject) jsonElement;

        upgradeRecommended = jObject.getAsJsonPrimitive("upgradeRecommended").getAsBoolean();
        upgradeRequired = jObject.getAsJsonPrimitive("upgradeRequired").getAsBoolean();

        Config config = new Config.Builder()
                .setUpgradeRecommended(upgradeRecommended)
                .setUpgradeRequired(upgradeRequired)
                .build();

        return config;
    }
}

