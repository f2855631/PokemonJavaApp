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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder> {
    private final List<Pokemon> pokemonList;
    private final Context context;
    private final SharedPreferences prefs;
    private final Set<String> caughtSet;

    private static final Map<String, String> typeMap = new HashMap<String, String>() {{
        put("一般", "normal");
        put("火", "fire");
        put("水", "water");
        put("草", "grass");
        put("電", "electric");
        put("冰", "ice");
        put("格鬥", "fighting");
        put("毒", "poison");
        put("地面", "ground");
        put("飛行", "flying");
        put("超能", "psychic");
        put("蟲", "bug");
        put("岩石", "rock");
        put("幽靈", "ghost");
        put("龍", "dragon");
        put("惡", "dark");
        put("鋼", "steel");
        put("妖精", "fairy");
    }};

    public PokemonAdapter(Context context, List<Pokemon> pokemonList) {
        this.context = context;
        this.pokemonList = pokemonList;
        this.prefs = context.getSharedPreferences("pokemonPrefs", Context.MODE_PRIVATE);
        this.caughtSet = new HashSet<>(prefs.getStringSet("caughtList", new HashSet<>())) ;
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

        holder.textName.setText(p.name);
        holder.textId.setText(String.format("%04d", Integer.parseInt(p.id)));

        if (p.type != null && !p.type.isEmpty()) {
            String type1Eng = typeMap.get(p.type.get(0));
            int resId1 = context.getResources().getIdentifier(type1Eng, "drawable", context.getPackageName());
            holder.imageType1.setImageResource(resId1);

            if (p.type.size() > 1) {
                holder.imageType2.setVisibility(View.VISIBLE);
                String type2Eng = typeMap.get(p.type.get(1));
                int resId2 = context.getResources().getIdentifier(type2Eng, "drawable", context.getPackageName());
                holder.imageType2.setImageResource(resId2);
            } else {
                holder.imageType2.setVisibility(View.GONE);
            }
        } else {
            holder.imageType1.setVisibility(View.GONE);
            holder.imageType2.setVisibility(View.GONE);
        }

        String imageUrl = "https://raw.githubusercontent.com/f2855631/pokemon-crawler/main/" + p.image;
        Glide.with(context).load(imageUrl).into(holder.imagePokemon);

        if (caughtSet.contains(p.id)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#D0F0C0"));
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PokemonDetailActivity.class);
            intent.putExtra("pokemon", p);
            context.startActivity(intent);
        });

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
        TextView textId, textName;
        ImageView imageType1, imageType2;

        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePokemon = itemView.findViewById(R.id.img_pokemon);
            textId = itemView.findViewById(R.id.tv_pokemon_number);
            textName = itemView.findViewById(R.id.tv_pokemon_name);
            imageType1 = itemView.findViewById(R.id.img_type1);
            imageType2 = itemView.findViewById(R.id.img_type2);
        }
    }
}
