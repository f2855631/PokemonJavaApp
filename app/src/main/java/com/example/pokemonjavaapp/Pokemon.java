package com.example.pokemonjavaapp;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Pokemon implements Parcelable {
    public String id;
    public int sub_id;
    public String name;
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
        category = in.readString();
        gender = in.readString();
        height = in.readString();
        weight = in.readString();
        abilities = in.createStringArrayList();
        weakness = in.createStringArrayList();
        type = in.createStringArrayList();
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
        parcel.writeString(category);
        parcel.writeString(gender);
        parcel.writeString(height);
        parcel.writeString(weight);
        parcel.writeStringList(abilities);
        parcel.writeStringList(weakness);
        parcel.writeStringList(type);
        parcel.writeString(image);
    }
}
