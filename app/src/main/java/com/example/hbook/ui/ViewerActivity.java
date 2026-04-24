package com.example.hbook.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.hbook.R;
import com.example.hbook.data.AppDatabase;
import com.example.hbook.data.LibraryDao;
import com.example.hbook.model.Page;

import java.util.ArrayList;
import java.util.List;

public class ViewerActivity extends AppCompatActivity {

    private boolean isTopBarVisible = true;
    private LibraryDao libraryDao;
    private View topBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        libraryDao = AppDatabase.getInstance(this).libraryDao();

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        topBar = findViewById(R.id.top_bar);
        TextView tvBack = findViewById(R.id.tv_back);
        TextView tvBookTitle = findViewById(R.id.tv_viewer_title);

        tvBack.setOnClickListener(v -> finish());

        int bookId = getIntent().getIntExtra("BOOK_ID", -1);
        String bookName = getIntent().getStringExtra("BOOK_NAME");
        
        if (bookName != null) {
            tvBookTitle.setText(bookName);
        }

        List<Page> pagesToDisplay = new ArrayList<>();

        // 텍스트뷰에 내용 채우기
        if (bookId != -1) {
            pagesToDisplay = libraryDao.getPagesForBook(bookId);
        }

        if (pagesToDisplay.isEmpty()) {
            pagesToDisplay.add(new Page(-1, 1, "저장된 내용이 없습니다."));
        }

        PageAdapter adapter = new PageAdapter(pagesToDisplay, this::toggleTopBar);
        viewPager.setAdapter(adapter);
    }

    private void toggleTopBar() {
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