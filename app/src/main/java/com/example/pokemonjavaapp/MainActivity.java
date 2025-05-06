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
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PokemonAdapter adapter;
    private List<Pokemon> fullPokemonList = new ArrayList<>();
    private List<Pokemon> originalList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.pokemonRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

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
        });
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
}
