package com.example.homework1.models;

import androidx.annotation.NonNull;

public class Trainer {
    private String name = "";
    private int afekaMons;

    public Trainer() {
    }

    public String getName() {
        return this.name;
    }

    public Trainer setName(String name) {
        this.name = name;
        return this;
    }

    public int getAfekaMons() {
        return this.afekaMons;
    }

    public Trainer setAfekaMons(int count) {
        this.afekaMons = count;
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return "Trainer{" +
                ", name='" + name + '\'' +
                ", afekaMons='" + afekaMons +
                '}';
    }
}
