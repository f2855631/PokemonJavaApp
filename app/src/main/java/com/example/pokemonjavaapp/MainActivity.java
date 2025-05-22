// MainActivity - 寶可夢 App 主畫面邏輯，負責初始化 UI、載入資料、處理篩選功能
package com.example.pokemonjavaapp;

// 匯入所需的 Android 與第三方函式庫
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
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

    // 宣告畫面元素與資料列表
    private RecyclerView recyclerView; // 顯示寶可夢的 RecyclerView
    private PokemonAdapter adapter; // 自訂的資料轉換器
    private List<Pokemon> fullPokemonList = new ArrayList<>(); // 所有寶可夢資料
    private List<Pokemon> originalList = new ArrayList<>(); // 原始資料（供搜尋、篩選使用）
    private ProgressBar progressBar; // 資料載入時的進度條

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 UI 元件與功能模組
        setupStatusBar();      // 狀態列樣式
        setupRecyclerView();   // RecyclerView 初始化
        setupSearchView();     // 搜尋列設定
        setupMenu();           // 篩選選單設定
        setupSwipeRefresh();   // 下拉更新設定

        progressBar.setVisibility(View.VISIBLE);
        loadPokemonData();     // 從遠端載入寶可夢資料
    }

    // 設定狀態列顏色與文字亮色
    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#B8E8D2"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    // 初始化 RecyclerView 與資料適配器
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.pokemonRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        int spanCount = calculateSpanCount(); // 根據螢幕寬度動態計算顯示欄數
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        adapter = new PokemonAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);
        new me.zhanghai.android.fastscroll.FastScrollerBuilder(recyclerView).build(); // 加入快速捲動功能
    }

    // 下拉更新設定（SwipeRefreshLayout）
    private void setupSwipeRefresh() {
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        // 當滑動到頂部才啟用下拉更新
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                swipeRefreshLayout.setEnabled(!rv.canScrollVertically(-1));
            }
        });
        // 下拉後觸發重新載入資料
        swipeRefreshLayout.setOnRefreshListener(() -> {
            progressBar.setVisibility(View.VISIBLE);
            loadPokemonData();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    // 搜尋欄設定：包含顯示樣式與文字輸入監聽
    private void setupSearchView() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("使用名稱或圖鑑編號搜尋");

        View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_plate);
        if (searchPlate != null) searchPlate.setBackgroundColor(Color.TRANSPARENT);

        EditText searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchText != null) {
            searchText.setTextColor(Color.BLACK);
            searchText.setHintTextColor(Color.GRAY);
        }

        // 搜尋文字輸入行為
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPokemon(query); // 送出時直接搜尋
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPokemon(newText); // 每輸入一字即時過濾
                return true;
            }
        });
    }
    // 點擊空白處時收起鍵盤與清除焦點的快速處理方式
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() != MotionEvent.ACTION_DOWN) return super.dispatchTouchEvent(ev);

        View v = getCurrentFocus();
        if (!(v instanceof EditText)) return super.dispatchTouchEvent(ev);

        Rect outRect = new Rect();
        v.getGlobalVisibleRect(outRect);
        if (outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) return super.dispatchTouchEvent(ev);

        v.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        return super.dispatchTouchEvent(ev);
    }

    // 設定篩選選單功能
    private void setupMenu() {
        ImageView menuIcon = findViewById(R.id.menu_filter);
        menuIcon.setOnClickListener(v -> showFilterMenu());
    }

    // 計算畫面可容納幾欄寶可夢卡片
    private int calculateSpanCount() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return Math.max(2, (int) (metrics.widthPixels / metrics.density / 180));
    }

    // 透過 PokemonFetcher 抓取資料，載入成功後更新畫面
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

    // 根據使用者輸入篩選寶可夢（可用名稱、編號或屬性）
    public void filterPokemon(String query) {
        if (adapter == null || originalList == null) return;

        String trimmed = query.trim().toLowerCase();
        if (trimmed.isEmpty()) {
            adapter.updateList(new ArrayList<>(originalList));
            return;
        }

        List<Pokemon> filtered = new ArrayList<>();
        for (Pokemon p : originalList) {
            boolean nameMatch = p.name != null && p.name.toLowerCase().contains(trimmed);
            boolean idMatch = p.id != null && p.id.contains(trimmed.replace("#", ""));
            boolean typeMatch = p.type != null && p.type.stream().anyMatch(t -> t != null && t.toLowerCase().contains(trimmed));

            if (nameMatch || idMatch || typeMatch) filtered.add(p);
        }
        adapter.updateList(filtered);
    }

    // 將文字加上樣式（置中、加大、粗體），供 PopupMenu 使用
    private SpannableString makeStyledText(String text) {
        SpannableString ss = new SpannableString(text);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new RelativeSizeSpan(1.3f), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    // 顯示彈出式篩選選單
    private void showFilterMenu() {
        PopupMenu popup = new PopupMenu(this, findViewById(R.id.menu_filter), Gravity.END);
        String[] options = {"主畫面", "已收服", "未收服", "世代/地區", "Mega進化", "超極巨化", "其他型態"};
        for (String option : options) popup.getMenu().add(makeStyledText(option));

        popup.setOnMenuItemClickListener(item -> handleMenuSelection(item.getTitle().toString().trim()));
        popup.show();
    }

    // 根據選單文字內容執行對應篩選行為
    private boolean handleMenuSelection(String title) {
        if (adapter == null) return false;

        switch (title) {
            case "主畫面":
                adapter.updateList(new ArrayList<>(originalList));
                return true;
            case "世代/地區":
                fetchGenerationListAndShowDialog();
                return true;
            case "Mega進化":
            case "超極巨化":
                filterFormType(title.equals("Mega進化") ? "mega" : "gmax");
                return true;
            case "其他型態":
                filterOtherForms();
                return true;
            case "已收服":
            case "未收服":
                filterByCaughtStatus(title);
                return true;
            default:
                return false;
        }
    }

    // 篩選「其他型態」的寶可夢（排除已知 mega、gmax、地區型）
    private void filterOtherForms() {
        List<Pokemon> result = new ArrayList<>();
        for (Pokemon p : originalList) {
            boolean hasForm = p.form_name != null && !p.form_name.trim().isEmpty();
            String type = p.form_type != null ? p.form_type.toLowerCase() : "";
            boolean excluded = type.matches("mega|gmax|alola|galar|hisui|paldea");
            if (hasForm && !excluded) result.add(p);
        }
        adapter.updateList(result);
    }

    // 根據 SharedPreferences 記錄的收服狀態進行篩選
    private void filterByCaughtStatus(String status) {
        SharedPreferences prefs = getSharedPreferences("pokemonPrefs", MODE_PRIVATE);
        Set<String> caughtSet = prefs.getStringSet("caughtList", new HashSet<>());
        boolean filterCaught = status.contains("已收服");

        List<Pokemon> filtered = new ArrayList<>();
        for (Pokemon p : originalList) {
            String key = p.id + "-" + p.sub_id;
            boolean isCaught = caughtSet.contains(key);
            if ((filterCaught && isCaught) || (!filterCaught && !isCaught)) filtered.add(p);
        }
        adapter.updateList(filtered);
    }

    // 根據型態關鍵字（mega、gmax）過濾寶可夢
    private void filterFormType(String keyword) {
        List<Pokemon> filtered = new ArrayList<>();
        for (Pokemon p : originalList) {
            if (p.form_type != null && p.form_type.toLowerCase().contains(keyword)) {
                filtered.add(p);
            }
        }
        adapter.updateList(filtered);
    }

    // 從 GitHub 載入分類資料並顯示選擇彈窗
    private void fetchGenerationListAndShowDialog() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://raw.githubusercontent.com/f2855631/pokemon-crawler/refs/heads/main/pokemon_generations.json").build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "分類載入失敗", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;

                GenerationCategory[] data = new Gson().fromJson(response.body().string(), GenerationCategory[].class);
                List<String> labels = new ArrayList<>();
                for (GenerationCategory g : data) {
                    labels.add(g.generation + "/" + g.region.replaceAll("\\s*\\(.*?\\)", "") + "地區");
                }
                runOnUiThread(() -> showGenerationDialog(labels, data));
            }
        });
    }

    // 顯示世代分類對話框，供使用者選擇分類範圍
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
                TextView tv = view.findViewById(android.R.id.text1);
                tv.setTextSize(20);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setTextColor(Color.BLACK);
                tv.setGravity(Gravity.CENTER);
                tv.setBackgroundResource(R.drawable.bg_dialog_list);
                tv.setPadding(32, 32, 32, 32);
                return view;
            }
        };

        builder.setAdapter(adapter, (dialog, which) -> filterPokemonByRange(generationList[which].nationalRange));
        builder.show();
    }

    // 根據分類中的圖鑑編號範圍過濾寶可夢
    private void filterPokemonByRange(String range) {
        String[] parts = range.replace("#", "").split(" - ");
        int start = Integer.parseInt(parts[0]);
        int end = Integer.parseInt(parts[1]);

        List<Pokemon> result = new ArrayList<>();
        for (Pokemon p : originalList) {
            int num = Integer.parseInt(p.id);
            if (num >= start && num <= end) result.add(p);
        }
        adapter.updateList(result);
    }

    // 對應分類 JSON 的資料結構
    public static class GenerationCategory {
        @SerializedName("世代") public String generation;
        @SerializedName("地區") public String region;
        @SerializedName("全國編號範圍") public String nationalRange;
        @SerializedName("遊戲版本") public String gameVersions;
        @SerializedName("特色") public String feature;
    }
}
