package com.example.pokemonjavaapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder> {
    private final List<Pokemon> pokemonList;
    private final Context context;
    private final SharedPreferences prefs;
    private final Set<String> caughtSet;

    public PokemonAdapter(Context context, List<Pokemon> pokemonList) {
        this.context = context;
        this.pokemonList = pokemonList;
        this.prefs = context.getSharedPreferences("pokemonPrefs", Context.MODE_PRIVATE);
        this.caughtSet = new HashSet<>(prefs.getStringSet("caughtList", new HashSet<>()));
    }

    @NonNull
    @Override
    public PokemonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pokemon_card, parent, false);
        return new PokemonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokemonViewHolder holder, int position) {
        Pokemon p = pokemonList.get(position);

        // 顯示名稱
        holder.textName.setText(p.name);

        // 顯示編號
        holder.textId.setText(String.format("%04d", Integer.parseInt(p.id)));

        // 顯示屬性，List轉成字串
        if (p.type != null && !p.type.isEmpty()) {
            holder.textType.setText(String.join(" / ", p.type));
        } else {
            holder.textType.setText("");
        }

        // 顯示圖片
        String imageUrl = "https://raw.githubusercontent.com/f2855631/pokemon-crawler/main/" + p.image;
        Glide.with(context).load(imageUrl).into(holder.imagePokemon);

        // 顯示是否已收服
        if (caughtSet.contains(p.id)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#D0F0C0")); // 淺綠色
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        // 點一下進入詳細頁
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PokemonDetailActivity.class);
            intent.putExtra("pokemon", p);
            context.startActivity(intent);
        });

        // 長按標記已收服
        holder.itemView.setOnLongClickListener(v -> {
            toggleCaught(p);
            notifyItemChanged(position);
            return true;
        });
    }

    private void toggleCaught(Pokemon p) {
        if (caughtSet.contains(p.id)) {
            caughtSet.remove(p.id);
            Toast.makeText(context, "❌ 已取消收服：" + p.name, Toast.LENGTH_SHORT).show();
        } else {
            caughtSet.add(p.id);
            Toast.makeText(context, "✅ 已收服：" + p.name, Toast.LENGTH_SHORT).show();
        }
        prefs.edit().putStringSet("caughtList", caughtSet).apply();
    }

    public Set<String> getCaughtSet() {
        return new HashSet<>(caughtSet);
    }

    @Override
    public int getItemCount() {
        return pokemonList.size();
    }

    public void updateList(List<Pokemon> newList) {
        pokemonList.clear();
        pokemonList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class PokemonViewHolder extends RecyclerView.ViewHolder {
        ImageView imagePokemon;
        TextView textId, textType, textName; // ← 加上名字這一行

        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePokemon = itemView.findViewById(R.id.img_pokemon);
            textId = itemView.findViewById(R.id.tv_pokemon_number);
            textType = itemView.findViewById(R.id.tv_pokemon_types);
            textName = itemView.findViewById(R.id.tv_pokemon_name); // ← 這邊綁定名字
        }
    }
}
