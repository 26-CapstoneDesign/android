package com.example.hbook.ui;

import android.os.Bundle;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
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
    private View topBar;
    private ViewPager2 viewPager;

    // 사용자 뷰 설정 (실제로는 DB에서 가져와야 함)
    float userFontSize = 20f;
    float userLineSpacing = 1.5f;
    int userFontColor = 0xFF000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        viewPager = findViewById(R.id.view_pager);
        topBar = findViewById(R.id.top_bar);
        TextView tvBack = findViewById(R.id.tv_back);
        TextView tvBookTitle = findViewById(R.id.tv_viewer_title);

        tvBack.setOnClickListener(v -> finish());

        // 데이터 받기
        int bookId = getIntent().getIntExtra("BOOK_ID", -1);
        String bookName = getIntent().getStringExtra("BOOK_NAME");
        if (bookName != null) {
            tvBookTitle.setText(bookName);
        }

        // DB에서 모든 텍스트 가져와서 합치기
        LibraryDao libraryDao = AppDatabase.getInstance(this).libraryDao();
        List<Page> dbPages = new ArrayList<>();
        if (bookId != -1) {
            dbPages = libraryDao.getPagesForBook(bookId);
        }

        StringBuilder fullTextBuilder = new StringBuilder();
        for (Page p : dbPages) {
            fullTextBuilder.append(p.extractedText).append("\n\n");
        }
        String fullText = fullTextBuilder.toString().trim();

        if (fullText.isEmpty()) {
            fullText = "저장된 내용이 없습니다.";
        }

        // 화면의 실제 크기에 맞춰 자르기
        final String finalText = fullText;
        viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                paginateTextAndSetAdapter(finalText, userFontSize, userLineSpacing, userFontColor);
            }
        });
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

    private void paginateTextAndSetAdapter(String fullText, float fontSize, float lineSpacing, int fontColor) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, fontSize, getResources().getDisplayMetrics()));
        paint.setColor(fontColor);
        paint.setAntiAlias(true);

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, getResources().getDisplayMetrics());
        int availableWidth = viewPager.getWidth() - (padding * 2);
        int availableHeight = viewPager.getHeight() - (padding * 2);

        StaticLayout layout = StaticLayout.Builder.obtain(fullText, 0, fullText.length(), paint, availableWidth)
                .setLineSpacing(0, lineSpacing)
                .build();

        List<String> paginatedStrings = new ArrayList<>();
        int lineCount = layout.getLineCount();
        int currentLine = 0;

        while (currentLine < lineCount) {
            int startLine = currentLine;
            int pageHeight = 0;

            while (currentLine < lineCount) {
                int lineHeight = layout.getLineBottom(currentLine) - layout.getLineTop(currentLine);
                if (pageHeight + lineHeight > availableHeight) {
                    break;
                }
                pageHeight += lineHeight;
                currentLine++;
            }

            int startOffset = layout.getLineStart(startLine);
            int endOffset = layout.getLineEnd(currentLine - 1);

            paginatedStrings.add(fullText.substring(startOffset, endOffset));
        }

        PageAdapter adapter = new PageAdapter(paginatedStrings, fontSize, lineSpacing, fontColor, this::toggleTopBar);
        viewPager.setAdapter(adapter);
    }
}