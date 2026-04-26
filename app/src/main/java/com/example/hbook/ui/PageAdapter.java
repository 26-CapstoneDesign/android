package com.example.hbook.ui;

import android.util.TypedValue;
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
    private List<String> pageList;
    private OnPageClickListener listener;

    private float fontSize;
    private float lineSpacing;
    private int fontColor;

    public interface OnPageClickListener {
        void onPageClick();
    }

    public PageAdapter(List<String> pageList, float fontSize, float lineSpacing, int fontColor, OnPageClickListener listener) {
        this.pageList = pageList;
        this.fontSize = fontSize;
        this.lineSpacing = lineSpacing;
        this.fontColor = fontColor;
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
        holder.tvContent.setText(pageList.get(position));

        holder.tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        holder.tvContent.setTextColor(fontColor);
        holder.tvContent.setLineSpacing(0, lineSpacing);

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
