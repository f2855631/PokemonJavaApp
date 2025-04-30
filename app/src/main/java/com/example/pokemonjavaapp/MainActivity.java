package com.example.pokemonjavaapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

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

        Button btnBack = findViewById(R.id.btnBack);
        Button btnCaught = findViewById(R.id.btnCaught);
        Button btnUncaught = findViewById(R.id.btnUncaught);

        PokemonFetcher.fetchPokemonData(new PokemonFetcher.OnDataFetched() {
            @Override
            public void onSuccess(List<Pokemon> pokemonList) {
                fullPokemonList = new ArrayList<>(pokemonList);
                originalList = new ArrayList<>(pokemonList); // 存原始資料
                adapter = new PokemonAdapter(MainActivity.this, fullPokemonList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(String error) {
                Log.e("POKEMON", "抓資料失敗: " + error);
            }
        });

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

        btnBack.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.updateList(new ArrayList<>(originalList));
            }
        });

        btnCaught.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("pokemonPrefs", MODE_PRIVATE);
            Set<String> caughtSet = new HashSet<>(prefs.getStringSet("caughtList", new HashSet<>()));

            List<Pokemon> caughtList = new ArrayList<>();
            for (Pokemon p : originalList) {
                if (caughtSet.contains(p.id)) {
                    caughtList.add(p);
                }
            }
            adapter.updateList(caughtList);
        });

        btnUncaught.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("pokemonPrefs", MODE_PRIVATE);
            Set<String> caughtSet = new HashSet<>(prefs.getStringSet("caughtList", new HashSet<>()));

            List<Pokemon> uncaughtList = new ArrayList<>();
            for (Pokemon p : originalList) {
                if (!caughtSet.contains(p.id)) {
                    uncaughtList.add(p);
                }
            }
            adapter.updateList(uncaughtList);
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