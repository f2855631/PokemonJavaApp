package com.example.pokemonjavaapp;

import android.os.Handler;
import android.os.Looper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PokemonFetcher {
    private static final String JSON_URL = "https://raw.githubusercontent.com/f2855631/pokemon-crawler/refs/heads/main/pokemon_data.json";


    public interface OnDataFetched {
        void onSuccess(List<Pokemon> pokemonList);
        void onFailure(String error);
    }

    public static void fetchPokemonData(OnDataFetched callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(JSON_URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("HTTP Error: " + response.code());
                    return;
                }

                String json = response.body().string();
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Pokemon>>() {}.getType();
                List<Pokemon> list = gson.fromJson(json, listType);

                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(list));
            }
        });
    }
}

