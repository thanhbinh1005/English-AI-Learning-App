package com.thanhbinh.englishaiapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thanhbinh.englishaiapp.data.local.entity.VocabularyEntity;
import com.thanhbinh.englishaiapp.databinding.ItemVocabularyBinding;

import java.util.ArrayList;
import java.util.List;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> {

    private List<VocabularyEntity> vocabularyList = new ArrayList<>();
    private final OnVocabularyClickListener listener;

    public interface OnVocabularyClickListener {
        void onDeleteClick(VocabularyEntity vocabulary);
        void onSpeechClick(VocabularyEntity vocabulary);
        void onLearnedToggle(VocabularyEntity vocabulary, boolean isLearned);
    }

    public VocabularyAdapter(OnVocabularyClickListener listener) {
        this.listener = listener;
    }

    public void setVocabularies(List<VocabularyEntity> vocabularies) {
        this.vocabularyList = (vocabularies != null) ? vocabularies : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VocabularyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVocabularyBinding binding = ItemVocabularyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VocabularyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabularyViewHolder holder, int position) {
        holder.bind(vocabularyList.get(position));
    }

    @Override
    public int getItemCount() {
        return vocabularyList.size();
    }

    class VocabularyViewHolder extends RecyclerView.ViewHolder {
        private final ItemVocabularyBinding binding;

        VocabularyViewHolder(ItemVocabularyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(VocabularyEntity item) {
            binding.txtTerm.setText(item.getTerm());
            binding.txtMeaning.setText(item.getMeaning());

            binding.chkLearned.setOnCheckedChangeListener(null);
            binding.chkLearned.setChecked(item.isLearned());

            binding.chkLearned.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) listener.onLearnedToggle(item, isChecked);
            });

            if (item.getExample() != null && !item.getExample().trim().isEmpty()) {
                binding.txtExample.setText("Ví dụ: " + item.getExample());
                binding.txtExample.setVisibility(View.VISIBLE);
            } else {
                binding.txtExample.setVisibility(View.GONE);
            }

            binding.btnSpeech.setOnClickListener(v -> {
                if (listener != null) listener.onSpeechClick(item);
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(item);
            });
        }
    }
}
