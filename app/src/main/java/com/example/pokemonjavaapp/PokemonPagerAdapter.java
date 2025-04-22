package com.example.pokemonjavaapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import java.util.List;

public class PokemonPagerAdapter extends RecyclerView.Adapter<PokemonPagerAdapter.PokemonViewHolder> {

    private final Context context;
    private List<Pokemon> pokemonList;

    public PokemonPagerAdapter(Context context, List<Pokemon> pokemonList) {
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
        holder.textTypes.setText("屬性: " + String.join(", ", p.type));

        String imageUrl = "https://raw.githubusercontent.com/f2855631/pokemon-crawler/main/" + p.image;
        Glide.with(context).load(imageUrl).into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PokemonDetailActivity.class);
            intent.putExtra("pokemon", p);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return pokemonList.size();
    }

    public void updateList(List<Pokemon> newList) {
        this.pokemonList = newList;
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
