package com.thanhbinh.englishaiapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thanhbinh.englishaiapp.R;
import com.thanhbinh.englishaiapp.data.local.entity.HistoryItem;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryItem> historyList;

    public interface OnItemClickListener {
        void onItemClick(HistoryItem item);
        void onFavoriteClick(HistoryItem item);
    }

    private OnItemClickListener listener;

    public HistoryAdapter(List<HistoryItem> historyList, OnItemClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    public void updateData(List<HistoryItem> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        holder.tvLangHeader.setText(item.sourceLang != null ? item.sourceLang.toUpperCase() : "");
        holder.tvOriginalText.setText(item.sourceText);
        holder.ivFavorite.setImageResource(item.isFavorite ? R.drawable.ic_star_on : R.drawable.ic_star_off);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        holder.ivFavorite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLangHeader, tvOriginalText;
        ImageView ivFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLangHeader = itemView.findViewById(R.id.tvLangHeader);
            tvOriginalText = itemView.findViewById(R.id.tvOriginalText);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }
}
