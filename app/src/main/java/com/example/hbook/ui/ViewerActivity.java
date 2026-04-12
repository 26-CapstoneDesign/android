package com.example.hbook.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hbook.R;

public class ViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        TextView tvResult = findViewById(R.id.tv_ocr_result);

        // 카메라에서 넘겨준 텍스트를 받아서 텍스트뷰에 띄움
        String ocrText = getIntent().getStringExtra("OCR_TEXT");
        if (ocrText != null) {
            tvResult.setText(ocrText);
        }
    }
}