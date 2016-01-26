package ua.kaganovych.superlocationproject.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import ua.kaganovych.superlocationproject.Config;
import ua.kaganovych.superlocationproject.R;
import ua.kaganovych.superlocationproject.api.parser.DirectionParser;
import ua.kaganovych.superlocationproject.api.response.DirectionResponse;
import ua.kaganovych.superlocationproject.model.Direction;

public class ApiHelper {

    private Context context;
    private ApiInterface apiInterface;

    public ApiHelper(Context context) {

        this.context = context;

        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(DirectionResponse.class, new DirectionParser())
                .create();

        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Config.BASE_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(new OkClient(new OkHttpClient()))
                .setConverter(new GsonConverter(gson))
                .build();

        apiInterface = restAdapter.create(ApiInterface.class);
    }

    public void getDirection(Direction direction, Callback<DirectionResponse> callback) {
        final String origin = direction.fromLat + "," + direction.fromLon;
        final String destination = direction.toLat + "," + direction.toLon;
        apiInterface.getDirection(context.getString(R.string.direction_api_key), origin, destination, callback);
    }

}
