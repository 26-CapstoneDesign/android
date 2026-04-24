package com.example.hbook.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hbook.R;
import com.example.hbook.model.Page;

import java.util.List;

public class PageAdapter extends RecyclerView.Adapter<PageAdapter.PageViewHolder> {
    private List<Page> pageList;
    private OnPageClickListener listener;

    public interface OnPageClickListener {
        void onPageClick();
    }

    public PageAdapter(List<Page> pageList, OnPageClickListener listener) {
        this.pageList = pageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        Page page = pageList.get(position);
        holder.tvContent.setText(page.extractedText);

        holder.tvContent.setOnClickListener(v -> {
            if (listener != null) listener.onPageClick();
        });
    }

    @Override
    public int getItemCount() {
        return pageList != null ? pageList.size() : 0;
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_page_content);
        }
    }
}
