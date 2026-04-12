package com.example.hbook.ui;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hbook.R;
import com.example.hbook.model.Book;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<Book> bookList;

    public BookAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book currnetBook = bookList.get(position);

        holder.tvTitle.setText(currnetBook.title);

        if (currnetBook.isFavorite) {
            holder.ivFavorite.setImageResource(android.R.drawable.star_on);
            holder.ivFavorite.setColorFilter(Color.parseColor("#FFC107"));
        } else {
            holder.ivFavorite.setImageResource(android.R.drawable.star_off);
            holder.ivFavorite.setColorFilter(Color.parseColor("#BDBDBD"));
        }

        holder.ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPos = holder.getAdapterPosition();

                if (currentPos != RecyclerView.NO_POSITION) {
                    Book clickedBook = bookList.get(currentPos);
                    clickedBook.isFavorite = !clickedBook.isFavorite;
                    notifyItemChanged(currentPos);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPos = holder.getAdapterPosition();

                if (currentPos != RecyclerView.NO_POSITION) {
                    Book clickedBook = bookList.get(currentPos);
                    Intent intent = new Intent(v.getContext(), ViewerActivity.class);
                    String dummyText = currnetBook.title + "의 스캔 내용";
                    intent.putExtra("OCR_TEXT", dummyText);

                    v.getContext().startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivFavorite;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
        }
    }
}
