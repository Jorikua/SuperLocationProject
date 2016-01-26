package ua.kaganovych.superlocationproject.api.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import ua.kaganovych.superlocationproject.api.response.DirectionResponse;
import ua.kaganovych.superlocationproject.util.MapUitls;

public class DirectionParser implements JsonDeserializer<DirectionResponse> {
    @Override
    public DirectionResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        final DirectionResponse directionResponse = new DirectionResponse();
        final JsonObject jsonObject = json.getAsJsonObject();
        final JsonArray routesArray = jsonObject.getAsJsonArray("routes");
        for (int i = 0; i < routesArray.size(); i++) {
            final JsonElement jsonElement = routesArray.get(i);
            final JsonObject polylineObject = jsonElement.getAsJsonObject().getAsJsonObject("overview_polyline");
            directionResponse.polylineList = MapUitls.decode(polylineObject.get("points").getAsString());
        }

        return directionResponse;
    }
}
