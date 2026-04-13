package com.example.hbook.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "pages",
        foreignKeys = @ForeignKey(entity = Book.class,
                                  parentColumns = "id",
                                  childColumns = "bookId",
                                  onDelete = ForeignKey.CASCADE))
public class Page {
    @PrimaryKey(autoGenerate = true)
    public int pageId;

    public int bookId;
    public int pageNumber;

    // GPT 교정 텍스트
    public String extractedText;

    public float emotionValence;
    public float emotionArousal;

    public Page(int bookId, int pageNumber, String extractedText) {
        this.bookId = bookId;
        this.pageNumber = pageNumber;
        this.extractedText = extractedText;
    }
}
