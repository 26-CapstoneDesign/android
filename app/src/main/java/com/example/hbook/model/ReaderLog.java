package com.example.hbook.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "reader_logs",
        foreignKeys = @ForeignKey(entity = Book.class,
                                  parentColumns = "id",
                                  childColumns = "bookId",
                                  onDelete = ForeignKey.CASCADE))
public class ReaderLog {
    @PrimaryKey(autoGenerate = true)
    public int logId;

    public int bookId;
    public int pageNumber;

    public String logType;
    public int startIndex;
    public int endIndex;
    public String color;

    public ReaderLog(int bookId, int pageNumber, String logType, int startIndex, int endIndex, String color) {
        this.bookId = bookId;
        this.pageNumber = pageNumber;
        this.logType = logType;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.color = color;
    }
}
