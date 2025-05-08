package com.example.pokemonjavaapp;

import com.google.gson.annotations.SerializedName;

public class GenerationCategory {
    @SerializedName("世代")
    public String generation;

    @SerializedName("地區")
    public String region;

    @SerializedName("全國編號範圍")
    public String nationalRange;

    @SerializedName("遊戲版本")
    public String gameVersions;

    @SerializedName("特色")
    public String feature;
}
