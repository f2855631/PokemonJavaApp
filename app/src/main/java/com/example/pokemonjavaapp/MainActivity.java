package com.example.pokemonjavaapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private PokemonPagerAdapter adapter;

    private List<Pokemon> fullPokemonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        PokemonFetcher.fetchPokemonData(new PokemonFetcher.OnDataFetched() {
            @Override
            public void onSuccess(List<Pokemon> pokemonList) {
                fullPokemonList = pokemonList;
                adapter = new PokemonPagerAdapter(MainActivity.this, pokemonList);
                viewPager.setAdapter(adapter);
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
                filterPokemon(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    public void filterPokemon(String query) {
        if (adapter != null && fullPokemonList != null) {
            List<Pokemon> filtered = new java.util.ArrayList<>();
            for (Pokemon p : fullPokemonList) {
                if (p.name.contains(query) || p.id.contains(query)) {
                    filtered.add(p);
                }
            }
            adapter.updateList(filtered);
        }
    }
}
