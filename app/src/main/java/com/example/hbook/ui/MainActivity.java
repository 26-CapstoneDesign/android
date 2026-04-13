package com.example.hbook.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hbook.R;
import com.example.hbook.data.AppDatabase;
import com.example.hbook.model.Book;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvLibrary;
    private BookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvLibrary = findViewById(R.id.rv_library);
        rvLibrary.setLayoutManager(new GridLayoutManager(this, 3));

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(view -> showNameInputDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();

        AppDatabase db = AppDatabase.getInstance(this);

        List<Book> savedBooks = db.libraryDao().getAllBooks();

        adapter = new BookAdapter(savedBooks);
        rvLibrary.setAdapter(adapter);
    }

    private void showNameInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("새로운 책 추가");
        builder.setMessage("저장할 책의 이름을 입력해 주세요.");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = 64;
        params.rightMargin = 64;
        input.setLayoutParams(params);

        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String bookName = input.getText().toString();

                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra("BOOK_NAME", bookName);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}