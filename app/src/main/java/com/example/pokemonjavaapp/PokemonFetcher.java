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
    // JSON 資料來源網址（來自 GitHub）
    private static final String JSON_URL = "https://raw.githubusercontent.com/f2855631/pokemon-crawler/refs/heads/main/pokemon_data.json";

    // 定義回呼介面，用來回傳抓取結果
    public interface OnDataFetched {
        void onSuccess(List<Pokemon> pokemonList); // 成功時回傳資料清單
        void onFailure(String error); // 失敗時回傳錯誤訊息
    }

    // 抓取寶可夢資料的靜態方法
    public static void fetchPokemonData(OnDataFetched callback) {
        OkHttpClient client = new OkHttpClient(); // 建立 HTTP 客戶端
        Request request = new Request.Builder().url(JSON_URL).build(); // 建立 GET 請求

        // 非同步執行請求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 錯誤時回傳錯誤訊息到主執行緒
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 回應失敗（例如 404）
                if (!response.isSuccessful()) {
                    callback.onFailure("HTTP Error: " + response.code());
                    return;
                }

                // 成功後取得 JSON 字串
                String json = response.body().string();
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Pokemon>>() {}.getType();

                // 將 JSON 字串轉成 List<Pokemon> 物件
                List<Pokemon> list = gson.fromJson(json, listType);

                // 將結果回傳到主執行緒供 UI 使用
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(list));
            }
        });
    }
}