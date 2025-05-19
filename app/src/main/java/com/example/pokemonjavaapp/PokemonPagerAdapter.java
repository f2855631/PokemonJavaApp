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

import com.bumptech.glide.Glide;

import java.util.List;

// ✅ 寶可夢用的 RecyclerView 分頁 Adapter，顯示圖片與基本資訊，支援點擊跳轉詳情頁
public class PokemonPagerAdapter extends RecyclerView.Adapter<PokemonPagerAdapter.PokemonViewHolder> {

    private final Context context; // 上下文，用於載入圖片與啟動 Activity
    private List<Pokemon> pokemonList; // 顯示的寶可夢資料清單

    public PokemonPagerAdapter(Context context, List<Pokemon> pokemonList) {
        this.context = context;
        this.pokemonList = pokemonList;
    }

    // 建立 ViewHolder（載入卡片版面）
    @NonNull
    @Override
    public PokemonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pokemon_card, parent, false);
        return new PokemonViewHolder(view);
    }

    // 資料綁定到畫面元件
    @Override
    public void onBindViewHolder(@NonNull PokemonViewHolder holder, int position) {
        Pokemon p = pokemonList.get(position);

        // 設定名稱與編號與屬性
        holder.textName.setText(p.name);
        holder.textId.setText(p.id);
        holder.textTypes.setText("屬性: " + String.join(", ", p.type));

        // 使用 Glide 載入圖片
        String imageUrl = "https://raw.githubusercontent.com/f2855631/pokemon-crawler/main/" + p.image;
        Glide.with(context).load(imageUrl).into(holder.imageView);

        // 點擊跳轉至詳情頁面
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PokemonDetailActivity.class);
            intent.putExtra("pokemon", p);
            context.startActivity(intent);
        });
    }

    // 回傳資料總筆數
    @Override
    public int getItemCount() {
        return pokemonList.size();
    }

    // 更新列表資料（例如搜尋或分頁）
    public void updateList(List<Pokemon> newList) {
        this.pokemonList = newList;
        notifyDataSetChanged();
    }

    // ViewHolder 類別：綁定單一寶可夢卡片的 UI 元件
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