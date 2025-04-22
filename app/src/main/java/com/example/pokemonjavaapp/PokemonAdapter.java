package com.example.pokemonjavaapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder> {
    private final List<Pokemon> pokemonList;
    private final Context context;

    public PokemonAdapter(Context context, List<Pokemon> pokemonList) {
        this.context = context;
        this.pokemonList = pokemonList;
    }

    @NonNull
    @Override
    public PokemonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pokemon, parent, false);
        return new PokemonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokemonViewHolder holder, int position) {
        Pokemon p = pokemonList.get(position);
        holder.textName.setText(p.name);
        holder.textId.setText(p.id);
        holder.textTypes.setText("屬性: " +
                (p.type != null && !p.type.isEmpty() ? String.join(", ", p.type) : "無"));
        String imageUrl = "https://raw.githubusercontent.com/f2855631/pokemon-crawler/main/" + p.image;
        Log.d("IMAGE_URL", "Loading image from: " + imageUrl);
        Glide.with(context).load(imageUrl).into(holder.imageView);


        // 加上點擊事件：跳轉到 PokemonDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PokemonDetailActivity.class);
            intent.putExtra("pokemon", p); // 傳遞 Parcelable 寶可夢物件
            context.startActivity(intent);
        });
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
        ImageView imageView;
        TextView textName, textId, textTypes;

        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textName = itemView.findViewById(R.id.textName);
            textId = itemView.findViewById(R.id.textId);
            textTypes = itemView.findViewById(R.id.textTypes);
        }
    }
}

