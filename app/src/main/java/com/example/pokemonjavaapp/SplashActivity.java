package com.example.pokemonjavaapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // ❗️設定狀態列為白色背景（不改字色）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(0xFFFFFFFF); // 白色背景
        }

        // 顯示 1.5 秒後跳到主畫面
        new Handler().postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish(); // 不讓使用者返回這畫面
        }, 1500);
    }
}
