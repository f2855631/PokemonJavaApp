package com.example.pokemonjavaapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView; // 顯示寶可夢清單的 RecyclerView
    private PokemonAdapter adapter; // 自訂的 Adapter
    private List<Pokemon> fullPokemonList = new ArrayList<>(); // 全部寶可夢資料
    private List<Pokemon> originalList = new ArrayList<>(); // 原始列表（用於搜尋與重設）
    private ProgressBar progressBar; // 載入資料時顯示進度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 設定狀態列顏色與圖示樣式（馬卡龍綠 + 黑色文字）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#B8E8D2"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // 初始化畫面元件
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.pokemonRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // 根據螢幕寬度決定欄位數
        int spanCount = calculateSpanCount();
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        adapter = new PokemonAdapter(MainActivity.this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // 加入快速滾動功能
        new me.zhanghai.android.fastscroll.FastScrollerBuilder(recyclerView).build();

        // 滾動到頂時才允許下拉刷新
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                swipeRefreshLayout.setEnabled(!recyclerView.canScrollVertically(-1));
            }
        });

        // 下拉刷新時重新載入資料
        swipeRefreshLayout.setOnRefreshListener(() -> {
            progressBar.setVisibility(View.VISIBLE);
            loadPokemonData();
            swipeRefreshLayout.setRefreshing(false);
        });

        // 初始化搜尋欄樣式與監聽器
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(true);
        searchView.setQueryHint("使用名稱或圖鑑編號搜尋");

        View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_plate);
        if (searchPlate != null) searchPlate.setBackgroundColor(Color.TRANSPARENT);

        EditText searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchText != null) {
            searchText.setTextColor(Color.BLACK);
            searchText.setHintTextColor(Color.GRAY);
        }

        // 設定搜尋文字變更監聽
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPokemon(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPokemon(newText);
                return true;
            }
        });

        // 設定選單按鈕事件
        ImageView menuIcon = findViewById(R.id.menu_filter);
        menuIcon.setOnClickListener(v -> showFilterMenu());

        // 載入初始資料
        progressBar.setVisibility(View.VISIBLE);
        loadPokemonData();
    }

    // 根據螢幕寬度動態調整欄位數量
    private int calculateSpanCount() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return Math.max(2, (int) (dpWidth / 180));
    }

    // 從網路載入寶可夢資料
    private void loadPokemonData() {
        PokemonFetcher.fetchPokemonData(new PokemonFetcher.OnDataFetched() {
            @Override
            public void onSuccess(List<Pokemon> pokemonList) {
                fullPokemonList = new ArrayList<>(pokemonList);
                originalList = new ArrayList<>(pokemonList);
                adapter.updateList(fullPokemonList);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String error) {
                Log.e("POKEMON", "抓資料失敗: " + error);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // 搜尋篩選邏輯：支援名稱、編號、屬性
    public void filterPokemon(String query) {
        if (adapter != null && originalList != null) {
            String trimmedQuery = query.trim();
            if (trimmedQuery.isEmpty()) {
                adapter.updateList(new ArrayList<>(originalList));
                return;
            }

            List<Pokemon> filtered = new ArrayList<>();
            String lowerQuery = trimmedQuery.toLowerCase();

            for (Pokemon p : originalList) {
                boolean matchName = p.name != null && p.name.toLowerCase().contains(lowerQuery);
                boolean matchId = p.id != null && p.id.contains(lowerQuery.replace("#", ""));
                boolean matchType = p.type != null && p.type.stream().anyMatch(t -> t != null && t.toLowerCase().contains(lowerQuery));

                if (matchName || matchId || matchType) {
                    filtered.add(p);
                }
            }

            adapter.updateList(filtered);
        }
    }

    // 建立彈出選單選項樣式（字體加粗、置中、放大）
    private SpannableString makeStyledText(String text) {
        SpannableString ss = new SpannableString(text);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new RelativeSizeSpan(1.3f), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    // 顯示分類選單邏輯
    private void showFilterMenu() {
        ImageView menuIcon = findViewById(R.id.menu_filter);
        PopupMenu popup = new PopupMenu(MainActivity.this, menuIcon, Gravity.END);

        // 加入選單項目
        popup.getMenu().add(makeStyledText("主畫面"));
        popup.getMenu().add(makeStyledText("已收服"));
        popup.getMenu().add(makeStyledText("未收服"));
        popup.getMenu().add(makeStyledText("世代/地區"));
        popup.getMenu().add(makeStyledText("Mega進化"));
        popup.getMenu().add(makeStyledText("超極巨化"));
        popup.getMenu().add(makeStyledText("其他型態"));

        // 設定每個選項的處理邏輯
        popup.setOnMenuItemClickListener(menuItem -> {
            String title = menuItem.getTitle().toString().replace(" ", "").trim();
            if (adapter == null) return false;

            if (title.contains("主畫面")) {
                adapter.updateList(new ArrayList<>(originalList));
            } else if (title.contains("世代/地區")) {
                fetchGenerationListAndShowDialog();
            } else if (title.contains("Mega進化")) {
                filterFormType("mega");
            } else if (title.contains("超極巨化")) {
                filterFormType("gmax");
            } else if (title.contains("其他型態")) {
                List<Pokemon> filtered = new ArrayList<>();
                for (Pokemon p : originalList) {
                    boolean hasFormName = p.form_name != null && !p.form_name.trim().isEmpty();
                    String formType = p.form_type != null ? p.form_type.toLowerCase() : "";
                    boolean isExcluded = formType.equals("mega") || formType.equals("gmax") ||
                            formType.equals("alola") || formType.equals("galar") ||
                            formType.equals("hisui") || formType.equals("paldea");
                    if (hasFormName && !isExcluded) {
                        filtered.add(p);
                    }
                }
                adapter.updateList(filtered);
            } else {
                SharedPreferences prefs = getSharedPreferences("pokemonPrefs", MODE_PRIVATE);
                Set<String> caughtSet = new HashSet<>(prefs.getStringSet("caughtList", new HashSet<>()))
                        ;
                List<Pokemon> resultList = new ArrayList<>();
                for (Pokemon p : originalList) {
                    String key = p.id + "-" + p.sub_id;
                    boolean isCaught = caughtSet.contains(key);
                    if ((title.contains("已收服") && isCaught) || (title.contains("未收服") && !isCaught)) {
                        resultList.add(p);
                    }
                }
                adapter.updateList(resultList);
            }
            return true;
        });

        popup.show();
    }

    // 篩選特定型態的寶可夢（例如 mega、gmax）
    private void filterFormType(String keyword) {
        List<Pokemon> filtered = new ArrayList<>();
        for (Pokemon p : originalList) {
            if (p.form_type != null && p.form_type.toLowerCase().contains(keyword)) {
                filtered.add(p);
            }
        }
        adapter.updateList(filtered);
    }

    // 從 GitHub 載入分類資料並顯示彈窗
    private void fetchGenerationListAndShowDialog() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://raw.githubusercontent.com/f2855631/pokemon-crawler/refs/heads/main/pokemon_generations.json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "分類載入失敗", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;

                String json = response.body().string();
                Gson gson = new Gson();
                GenerationCategory[] generationList = gson.fromJson(json, GenerationCategory[].class);

                List<String> displayList = new ArrayList<>();
                for (GenerationCategory g : generationList) {
                    String label = g.generation + "/" + g.region.replaceAll("\\s*\\(.*?\\)", "") + "地區";
                    displayList.add(label);
                }

                runOnUiThread(() -> showGenerationDialog(displayList, generationList));
            }
        });
    }

    // 顯示地區選擇彈窗
    private void showGenerationDialog(List<String> categories, GenerationCategory[] generationList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        TextView title = new TextView(this);
        title.setText("選擇分類");
        title.setBackgroundResource(R.drawable.bg_dialog_list);
        title.setPadding(40, 40, 40, 40);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);

        builder.setCustomTitle(title);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, categories) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextSize(20);
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                textView.setTextColor(Color.BLACK);
                textView.setGravity(Gravity.CENTER);
                textView.setBackgroundResource(R.drawable.bg_dialog_list);
                textView.setPadding(32, 32, 32, 32);
                return view;
            }
        };

        builder.setAdapter(adapter, (dialog, which) -> {
            GenerationCategory selected = generationList[which];
            filterPokemonByRange(selected.nationalRange);
        });

        builder.show();
    }

    // 根據世代/地區編號範圍篩選寶可夢
    private void filterPokemonByRange(String range) {
        String[] parts = range.replace("#", "").split(" - ");
        int start = Integer.parseInt(parts[0]);
        int end = Integer.parseInt(parts[1]);

        List<Pokemon> filtered = new ArrayList<>();
        for (Pokemon p : originalList) {
            int number = Integer.parseInt(p.id);
            if (number >= start && number <= end) {
                filtered.add(p);
            }
        }

        adapter.updateList(filtered);
    }

    // 世代資料結構類別
    public static class GenerationCategory {
        @SerializedName("世代")
        public String generation;

        @SerializedName("地區")
        public String region;

        @SerializedName("全國編號範圍")
        public String nationalRange;

        @SerializedName("遊戲版本")
        public String gameVersions;

        @SerializedName("特色")
        public String feature;
    }
}
