package com.example.pokemonjavaapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
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

    private RecyclerView recyclerView;
    private PokemonAdapter adapter;
    private List<Pokemon> fullPokemonList = new ArrayList<>();
    private List<Pokemon> originalList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.pokemonRecyclerView);

        int spanCount = calculateSpanCount();
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PokemonAdapter(MainActivity.this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        new me.zhanghai.android.fastscroll.FastScrollerBuilder(recyclerView)
                .setThumbDrawable(ContextCompat.getDrawable(this, R.drawable.fastscroll_thumb))
                .build();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (recyclerView.canScrollVertically(-1)) {
                    swipeRefreshLayout.setEnabled(false);
                } else {
                    swipeRefreshLayout.setEnabled(true);
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadPokemonData();
            swipeRefreshLayout.setRefreshing(false);
        });

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

        ImageView menuIcon = findViewById(R.id.menu_filter);
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, menuIcon, Gravity.END);
            popup.getMenu().add(makeStyledText("主畫面"));
            popup.getMenu().add(makeStyledText("已收服"));
            popup.getMenu().add(makeStyledText("未收服"));
            popup.getMenu().add(makeStyledText("世代/地區"));
            popup.getMenu().add(makeStyledText("Mega進化"));
            popup.getMenu().add(makeStyledText("超極巨化"));
            popup.getMenu().add(makeStyledText("其他型態"));

            popup.setOnMenuItemClickListener(menuItem -> {
                String title = menuItem.getTitle().toString().replace(" ", "").trim();
                if (adapter == null) return false;

                if (title.contains("主畫面")) {
                    adapter.updateList(new ArrayList<>(originalList));
                } else if (title.contains("世代/地區")) {
                    fetchGenerationListAndShowDialog();
                } else if (title.contains("Mega進化")) {
                    List<Pokemon> filtered = new ArrayList<>();
                    for (Pokemon p : originalList) {
                        if (p.form_type != null && p.form_type.toLowerCase().contains("mega")) {
                            filtered.add(p);
                        }
                    }
                    adapter.updateList(filtered);
                } else if (title.contains("超極巨化")) {
                    List<Pokemon> filtered = new ArrayList<>();
                    for (Pokemon p : originalList) {
                        if (p.form_type != null && p.form_type.toLowerCase().contains("gmax")) {
                            filtered.add(p);
                        }
                    }
                    adapter.updateList(filtered);
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
                    Set<String> caughtSet = new HashSet<>(prefs.getStringSet("caughtList", new HashSet<>())) ;
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
        });

        loadPokemonData();
    }

    private int calculateSpanCount() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int itemWidth = 180;
        return Math.max(2, (int) (dpWidth / itemWidth));
    }

    private void loadPokemonData() {
        PokemonFetcher.fetchPokemonData(new PokemonFetcher.OnDataFetched() {
            @Override
            public void onSuccess(List<Pokemon> pokemonList) {
                fullPokemonList = new ArrayList<>(pokemonList);
                originalList = new ArrayList<>(pokemonList);
                adapter.updateList(fullPokemonList);
            }

            @Override
            public void onFailure(String error) {
                Log.e("POKEMON", "抓資料失敗: " + error);
            }
        });
    }

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
                boolean matchType = false;

                if (p.type != null) {
                    for (String t : p.type) {
                        if (t != null && t.toLowerCase().contains(lowerQuery)) {
                            matchType = true;
                            break;
                        }
                    }
                }

                if (matchName || matchId || matchType) {
                    filtered.add(p);
                }
            }

            adapter.updateList(filtered);
        }
    }

    private SpannableString makeStyledText(String text) {
        SpannableString ss = new SpannableString(text);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new RelativeSizeSpan(1.3f), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

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
