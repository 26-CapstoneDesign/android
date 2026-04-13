package com.example.hbook.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "books")
public class Book {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public boolean isFavorite;
    public int lastReadPage;
    public long createdAt;

    public Book(String title) {
        this.title = title;
        this.isFavorite = false;
        this.lastReadPage = 1;
        this.createdAt = System.currentTimeMillis();
    }
}
