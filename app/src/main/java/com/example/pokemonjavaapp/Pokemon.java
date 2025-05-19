package com.example.pokemonjavaapp;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

// ✅ 寶可夢資料模型，用於儲存單一寶可夢的所有屬性，並支援 Parcelable 傳遞
public class Pokemon implements Parcelable {
    public String id;                // 編號（如 "0001"）
    public int sub_id;              // 子型態編號（如 0、1）
    public String name;             // 中文名稱
    public String form_name;        // 額外型態名稱（如「攻擊型態」）
    public String form_type;        // 型態分類（如 mega、gmax、alola）
    public String category;         // 分類（如「種子寶可夢」）
    public String gender;           // 性別（♂ / ♀ / 無）
    public String height;           // 身高（如 0.7m）
    public String weight;           // 體重（如 6.9kg）
    public List<String> abilities;  // 特性清單
    public List<String> weakness;   // 弱點屬性清單

    @SerializedName("types")
    public List<String> type;       // 屬性清單（如：草、毒）

    @SerializedName("image")
    public String image;            // 圖片相對路徑（如 images/0001_0.png）

    public Pokemon() {} // 預設建構子

    // Parcelable 建構子：從 Parcel 恢復資料
    protected Pokemon(Parcel in) {
        id = in.readString();
        sub_id = in.readInt();
        name = in.readString();
        form_type = in.readString();
        category = in.readString();
        gender = in.readString();
        height = in.readString();
        weight = in.readString();

        abilities = in.createStringArrayList();
        if (abilities == null) abilities = new ArrayList<>();

        weakness = in.createStringArrayList();
        if (weakness == null) weakness = new ArrayList<>();

        type = in.createStringArrayList();
        if (type == null) type = new ArrayList<>();

        image = in.readString();
    }

    // Parcelable 實作，供 Intent 傳遞使用
    public static final Creator<Pokemon> CREATOR = new Creator<Pokemon>() {
        @Override
        public Pokemon createFromParcel(Parcel in) {
            return new Pokemon(in);
        }

        @Override
        public Pokemon[] newArray(int size) {
            return new Pokemon[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // 將物件寫入 Parcel 中
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeInt(sub_id);
        parcel.writeString(name);
        parcel.writeString(form_type);
        parcel.writeString(category);
        parcel.writeString(gender);
        parcel.writeString(height);
        parcel.writeString(weight);
        parcel.writeStringList(abilities != null ? abilities : new ArrayList<>());
        parcel.writeStringList(weakness != null ? weakness : new ArrayList<>());
        parcel.writeStringList(type != null ? type : new ArrayList<>());
        parcel.writeString(image);
    }
}