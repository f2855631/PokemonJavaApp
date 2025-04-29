package com.example.pokemonjavaapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PokemonDetailActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textName, textId, textHeight, textWeight, textCategory, textGender, textAbilities, textWeaknesses, textType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_detail);

        // 綁定畫面元件
        imageView = findViewById(R.id.imageView);
        textName = findViewById(R.id.textName);
        textId = findViewById(R.id.textId);
        textHeight = findViewById(R.id.textHeight);
        textWeight = findViewById(R.id.textWeight);
        textCategory = findViewById(R.id.textCategory);
        textGender = findViewById(R.id.textGender);
        textAbilities = findViewById(R.id.textAbilities);
        textWeaknesses = findViewById(R.id.textWeaknesses);
        textType = findViewById(R.id.textTypes); // 對應 XML 中的 id

        // 取得傳進來的寶可夢物件
        Pokemon pokemon = getIntent().getParcelableExtra("pokemon");

        if (pokemon != null) {
            textName.setText(pokemon.name);
            textId.setText(pokemon.id);
            textType.setText("屬性: " +
                    (pokemon.type != null && !pokemon.type.isEmpty()
                            ? String.join(", ", pokemon.type)
                            : "無")); // ⬅ 注意這裡用的是 pokemon.type，不是 pokemon.types
            textHeight.setText("身高: " + pokemon.height);
            textWeight.setText("體重: " + pokemon.weight);
            textCategory.setText("分類: " + pokemon.category);
            textGender.setText("性別: " + pokemon.gender);

            textAbilities.setText("特性: " +
                    (pokemon.abilities != null && !pokemon.abilities.isEmpty()
                            ? String.join(", ", pokemon.abilities)
                            : "無"));

            textWeaknesses.setText("弱點: " +
                    (pokemon.weakness != null && !pokemon.weakness.isEmpty()
                            ? String.join(", ", pokemon.weakness)
                            : "無"));

            Button btnBackToMain = findViewById(R.id.btnBackToMain);
            btnBackToMain.setOnClickListener(v -> {
                finish(); // 👈 關閉當前畫面，回到 MainActivity
            });
            // 顯示圖片（從 github path 載入）
            String imageUrl = "https://raw.githubusercontent.com/f2855631/pokemon-crawler/main/" + pokemon.image;
            Log.d("DETAIL_IMAGE_URL", "載入圖片網址: " + imageUrl);
            Glide.with(this).load(imageUrl).into(imageView);

        }
    }
}
