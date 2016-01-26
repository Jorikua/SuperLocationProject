package ua.kaganovych.superlocationproject.api;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;
import ua.kaganovych.superlocationproject.api.response.DirectionResponse;

public interface ApiInterface {

    @GET("/directions/json")
    void getDirection(@Query("key") String apiKey,
                      @Query("origin") String origin,
                      @Query("destination") String destination,
                      Callback<DirectionResponse> callback);

}
