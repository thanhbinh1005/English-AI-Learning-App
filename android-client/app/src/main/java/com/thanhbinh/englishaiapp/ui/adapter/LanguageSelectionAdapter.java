package com.thanhbinh.englishaiapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thanhbinh.englishaiapp.R;

import java.util.List;

public class LanguageSelectionAdapter extends RecyclerView.Adapter<LanguageSelectionAdapter.ViewHolder> {

    public interface OnLanguageClickListener {
        void onLanguageClick(String language);
    }

    private List<String> languages;
    private OnLanguageClickListener listener;

    public LanguageSelectionAdapter(List<String> languages, OnLanguageClickListener listener) {
        this.languages = languages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String lang = languages.get(position);
        holder.tvLanguageName.setText(lang);
        holder.itemView.setOnClickListener(v -> listener.onLanguageClick(lang));
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLanguageName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLanguageName = itemView.findViewById(R.id.tvLanguageName);
        }
    }
}
