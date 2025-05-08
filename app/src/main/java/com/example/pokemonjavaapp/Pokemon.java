package com.example.pokemonjavaapp;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Pokemon implements Parcelable {
    public String id;
    public int sub_id;
    public String name;
    public String form_type;
    public String category;
    public String gender;
    public String height;
    public String weight;
    public List<String> abilities;
    public List<String> weakness;

    @SerializedName("types")
    public List<String> type;

    @SerializedName("image")
    public String image;

    public Pokemon() {}

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