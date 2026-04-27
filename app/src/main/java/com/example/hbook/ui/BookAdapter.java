package com.example.hbook.ui;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Delete;

import com.example.hbook.R;
import com.example.hbook.model.Book;
import com.example.hbook.model.Page;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<Book> bookList;
    private boolean isDeleteMode = false;
    private Set<Book> selectedBooks = new HashSet<>();

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int count);
    }
    private OnSelectionChangeListener selectionChangeListener;

    public BookAdapter(List<Book> bookList, OnSelectionChangeListener listener) {
        this.bookList = bookList;
        this.selectionChangeListener = listener;
    }

    public void setDeleteMode(boolean isDeleteMode) {
        this.isDeleteMode = isDeleteMode;
        this.selectedBooks.clear();
        notifyDataSetChanged();
    }

    public Set<Book> getSelectedBooks() {
        return selectedBooks;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.tvTitle.setText(book.title);

        if (book.isFavorite) {
            holder.ivFavorite.setImageResource(android.R.drawable.star_on);
            holder.ivFavorite.setColorFilter(Color.parseColor("#FFC107"));
        } else {
            holder.ivFavorite.setImageResource(android.R.drawable.star_off);
            holder.ivFavorite.setColorFilter(Color.parseColor("#BDBDBD"));
        }

        if (isDeleteMode) {
            holder.cbDelete.setVisibility(View.VISIBLE);
            holder.cbDelete.setChecked(selectedBooks.contains(book));
            holder.ivFavorite.setClickable(false);
        } else {
            holder.cbDelete.setVisibility(View.GONE);
            holder.cbDelete.setChecked(false);
            holder.ivFavorite.setClickable(true);
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

                    if (isDeleteMode) {
                        if (selectedBooks.contains(clickedBook)) {
                            selectedBooks.remove(clickedBook);
                            holder.cbDelete.setChecked(false);
                        } else {
                            selectedBooks.add(clickedBook);
                            holder.cbDelete.setChecked(true);
                        }

                        if (selectionChangeListener != null) {
                            selectionChangeListener.onSelectionChanged(selectedBooks.size());
                        }
                    } else {
                        Intent intent = new Intent(v.getContext(), ViewerActivity.class);
                        intent.putExtra("BOOK_ID", clickedBook.id);
                        intent.putExtra("BOOK_NAME", clickedBook.title);

                        v.getContext().startActivity(intent);
                    }
                }
            }
        });

        holder.cbDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    Book clickedBook = bookList.get(currentPos);
                    if (holder.cbDelete.isChecked()) {
                        selectedBooks.add(clickedBook);
                    } else {
                        selectedBooks.remove(clickedBook);
                    }

                    if (selectionChangeListener != null) {
                        selectionChangeListener.onSelectionChanged(selectedBooks.size());
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList != null ? bookList.size() : 0;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivFavorite;
        CheckBox cbDelete;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            cbDelete = itemView.findViewById(R.id.cb_delete);
        }
    }
}
