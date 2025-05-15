package com.example.pokemonjavaapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

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
        put("超能力", "psychic");
        put("蟲", "bug");
        put("岩石", "rock");
        put("幽靈", "ghost");
        put("龍", "dragon");
        put("惡", "dark");
        put("鋼", "steel");
        put("妖精", "fairy");
    }};

    private static final Map<String, String> formTypeMap = new HashMap<String, String>() {{
        put("alola", "阿羅拉的樣子");
        put("galar", "伽勒爾的樣子");
        put("hisui", "洗翠的樣子");
        put("paldea", "帕底亞的樣子");
        put("gmax", "超極巨化");
        put("mega", "Mega進化");
    }};

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull PokemonViewHolder holder, int position) {
        Pokemon p = pokemonList.get(position);

        holder.textName.setText(p.name);
        try {
            holder.textId.setText(String.format("%04d", Integer.parseInt(p.id)));
        } catch (NumberFormatException e) {
            holder.textId.setText(p.id);
        }

        String rawFormType = p.form_type != null ? p.form_type.trim().toLowerCase() : "";
        String displayFormType = formTypeMap.getOrDefault(rawFormType, "");
        String formName = p.form_name != null ? p.form_name.trim() : "";

        if (!displayFormType.isEmpty()) {
            holder.textFormType.setText(displayFormType);
            holder.textFormType.setVisibility(View.VISIBLE);
        } else if (!formName.isEmpty()) {
            holder.textFormType.setText(formName);
            holder.textFormType.setVisibility(View.VISIBLE);
        } else {
            holder.textFormType.setVisibility(View.GONE);
        }

        if (p.type != null && !p.type.isEmpty()) {
            String type1Eng = typeMap.getOrDefault(p.type.get(0), null);
            if (type1Eng != null) {
                int resId1 = context.getResources().getIdentifier(type1Eng, "drawable", context.getPackageName());
                if (resId1 != 0) {
                    holder.imageType1.setImageResource(resId1);
                    holder.imageType1.setVisibility(View.VISIBLE);
                } else {
                    holder.imageType1.setVisibility(View.GONE);
                }
            } else {
                holder.imageType1.setVisibility(View.GONE);
            }

            if (p.type.size() > 1) {
                String type2Eng = typeMap.getOrDefault(p.type.get(1), null);
                if (type2Eng != null) {
                    int resId2 = context.getResources().getIdentifier(type2Eng, "drawable", context.getPackageName());
                    if (resId2 != 0) {
                        holder.imageType2.setImageResource(resId2);
                        holder.imageType2.setVisibility(View.VISIBLE);
                    } else {
                        holder.imageType2.setVisibility(View.GONE);
                    }
                } else {
                    holder.imageType2.setVisibility(View.GONE);
                }
            } else {
                holder.imageType2.setVisibility(View.GONE);
            }
        } else {
            holder.imageType1.setVisibility(View.GONE);
            holder.imageType2.setVisibility(View.GONE);
        }

        String imageUrl = "https://raw.githubusercontent.com/f2855631/pokemon-crawler/main/" + p.image;
        Glide.with(context)
                .load(imageUrl)
                .override(90, 90)
                .centerCrop()
                .thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .placeholder(android.R.drawable.stat_sys_download)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.imagePokemon);

        String caughtKey = getCaughtKey(p);
        if (caughtSet.contains(caughtKey)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#D0F0C0"));
            holder.caughtStampImageView.setVisibility(View.VISIBLE);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.caughtStampImageView.setVisibility(View.GONE);
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

    private String getCaughtKey(Pokemon p) {
        return p.id + "-" + p.sub_id;
    }

    private void toggleCaught(Pokemon p) {
        String key = getCaughtKey(p);
        if (caughtSet.contains(key)) {
            caughtSet.remove(key);
        } else {
            caughtSet.add(key);
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
        ImageView imagePokemon, caughtStampImageView;
        TextView textId, textName, textFormType;
        ImageView imageType1, imageType2;

        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePokemon = itemView.findViewById(R.id.img_pokemon);
            textId = itemView.findViewById(R.id.tv_pokemon_number);
            textName = itemView.findViewById(R.id.tv_pokemon_name);
            textFormType = itemView.findViewById(R.id.tv_form_type);
            caughtStampImageView = itemView.findViewById(R.id.img_caught_stamp);
            imageType1 = itemView.findViewById(R.id.img_type1);
            imageType2 = itemView.findViewById(R.id.img_type2);
        }
    }
}
