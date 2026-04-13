package com.example.hbook.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hbook.R;
import com.example.hbook.data.AppDatabase;
import com.example.hbook.data.LibraryDao;
import com.example.hbook.model.Page;

import java.util.List;

public class ViewerActivity extends AppCompatActivity {

    private boolean isTopBarVisible = true;
    private LibraryDao libraryDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        libraryDao = AppDatabase.getInstance(this).libraryDao();

        TextView tvResult = findViewById(R.id.tv_ocr_result);
        View topBar = findViewById(R.id.top_bar);
        TextView tvBack = findViewById(R.id.tv_back);
        TextView tvBookTitle = findViewById(R.id.tv_viewer_title);

        int bookId = getIntent().getIntExtra("BOOK_ID", -1);
        String ocrText = getIntent().getStringExtra("OCR_TEXT");
        String bookName = getIntent().getStringExtra("BOOK_NAME");
        
        if (bookName != null) {
            tvBookTitle.setText(bookName);
        }

        // 텍스트뷰에 내용 채우기
        if (ocrText != null && !ocrText.isEmpty()) {
            tvResult.setText(ocrText);
        } else if (bookId != -1) {
            loadTextFromDatabase(bookId, tvResult);
        }

        // 뒤로가기
        tvBack.setOnClickListener(v -> finish());

        tvResult.setOnClickListener(v -> {
            if (isTopBarVisible) {
                topBar.animate()
                        .translationY(-topBar.getHeight())
                        .alpha(0.0f)
                        .setDuration(200)
                        .withEndAction(() -> topBar.setVisibility(View.GONE));
                isTopBarVisible = false;
            } else {
                topBar.setVisibility(View.VISIBLE);
                topBar.animate()
                        .translationY(0)
                        .alpha(1.0f)
                        .setDuration(200);
                isTopBarVisible = true;
            }
        });
    }

    private void loadTextFromDatabase(int bookId, TextView tvResult) {
        List<Page> pages = libraryDao.getPagesForBook(bookId);
        if (pages != null && !pages.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Page p : pages) {
                sb.append(p.extractedText).append("\n\n");
            }
            tvResult.setText(sb.toString().trim());
        } else {
            tvResult.setText("저장된 내용이 없습니다.");
        }
    }
}