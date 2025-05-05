package com.example.pokemonjavaapp;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private PokemonAdapter adapter;
    private List<Pokemon> fullPokemonList = new ArrayList<>();
    private List<Pokemon> originalList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar 設定
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // RecyclerView 初始化
        recyclerView = findViewById(R.id.pokemonRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // 資料抓取
        PokemonFetcher.fetchPokemonData(new PokemonFetcher.OnDataFetched() {
            @Override
            public void onSuccess(List<Pokemon> pokemonList) {
                fullPokemonList = new ArrayList<>(pokemonList);
                originalList = new ArrayList<>(pokemonList);
                adapter = new PokemonAdapter(MainActivity.this, fullPokemonList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(String error) {
                Log.e("POKEMON", "抓資料失敗: " + error);
            }
        });

        // 搜尋功能
        SearchView searchView = findViewById(R.id.searchView);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_filter) {

            View anchor = findViewById(R.id.menu_filter);
            if (anchor == null) anchor = toolbar;

            PopupMenu popup = new PopupMenu(this, anchor, Gravity.END);

            // 自定義字體樣式
            popup.getMenu().add(makeStyledText("主畫面"));
            popup.getMenu().add(makeStyledText("已收服"));
            popup.getMenu().add(makeStyledText("未收服"));

            // 反射啟用圖示（即使沒用 icon 也建議放上）
            try {
                Field mPopup = popup.getClass().getDeclaredField("mPopup");
                mPopup.setAccessible(true);
                Object menuPopupHelper = mPopup.get(popup);
                Method setForceShowIcon = menuPopupHelper.getClass()
                        .getDeclaredMethod("setForceShowIcon", boolean.class);
                setForceShowIcon.invoke(menuPopupHelper, true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            popup.setOnMenuItemClickListener(menuItem -> {
                String title = menuItem.getTitle().toString().replace(" ", "").trim();
                if (adapter == null) return false;

                if (title.contains("主畫面")) {
                    adapter.updateList(new ArrayList<>(originalList));
                } else {
                    SharedPreferences prefs = getSharedPreferences("pokemonPrefs", MODE_PRIVATE);
                    Set<String> caughtSet = new HashSet<>(prefs.getStringSet("caughtList", new HashSet<>()));
                    List<Pokemon> resultList = new ArrayList<>();
                    for (Pokemon p : originalList) {
                        boolean isCaught = caughtSet.contains(p.id);
                        if ((title.contains("已收服") && isCaught) || (title.contains("未收服") && !isCaught)) {
                            resultList.add(p);
                        }
                    }
                    adapter.updateList(resultList);
                }
                return true;
            });

            popup.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private SpannableString makeStyledText(String text) {
        SpannableString ss = new SpannableString(text);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new RelativeSizeSpan(1.3f), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
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
                boolean matchName = p.name.toLowerCase().contains(lowerQuery);
                boolean matchId = p.id.contains(lowerQuery.replace("#", ""));
                boolean matchType = false;

                if (p.type != null) {
                    for (String t : p.type) {
                        if (t.toLowerCase().contains(lowerQuery)) {
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
}
