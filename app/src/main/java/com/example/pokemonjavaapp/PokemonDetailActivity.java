package com.example.pokemonjavaapp;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PokemonDetailActivity extends AppCompatActivity {

    // 宣告畫面元件
    ImageView imageView;
    TextView textName, textId, textHeight, textWeight, textCategory, textGender, textAbilities, textWeaknesses, textType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_detail);

        // ✅ 將狀態列背景設為黑色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.BLACK);
        }

        // 返回主畫面的按鈕
        ImageButton btnBackToMain = findViewById(R.id.btnBackToMain);
        btnBackToMain.setOnClickListener(v -> finish());

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
        textType = findViewById(R.id.textTypes); // 注意：XML 中 id 應為 textTypes

        // 從 Intent 取得傳遞過來的寶可夢資料
        Pokemon pokemon = getIntent().getParcelableExtra("pokemon");

        if (pokemon != null) {
            // 設定寶可夢基本資訊到畫面上
            textName.setText(pokemon.name);
            textId.setText(pokemon.id);

            textType.setText("屬性: " +
                    (pokemon.type != null && !pokemon.type.isEmpty()
                            ? String.join(", ", pokemon.type)
                            : "無"));

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

            // 使用 Glide 載入圖片（來自 GitHub image 路徑）
            String imageUrl = "https://raw.githubusercontent.com/f2855631/pokemon-crawler/main/" + pokemon.image;
            Log.d("DETAIL_IMAGE_URL", "載入圖片網址: " + imageUrl);
            Glide.with(this).load(imageUrl).into(imageView);
        }
    }

    // 返回支援（點選返回鍵也能關閉）
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}