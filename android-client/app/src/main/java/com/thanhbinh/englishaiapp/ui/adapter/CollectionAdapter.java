package com.thanhbinh.englishaiapp.ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thanhbinh.englishaiapp.data.model.CollectionWithCount;
import com.thanhbinh.englishaiapp.databinding.ItemAddCollectionBinding;
import com.thanhbinh.englishaiapp.databinding.ItemCollectionBinding;

import java.util.ArrayList;
import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ADD_NEW = 0;
    private static final int TYPE_COLLECTION = 1;

    private List<CollectionWithCount> collectionList = new ArrayList<>();
    private final OnCollectionClickListener listener;

    public interface OnCollectionClickListener {
        void onAddNewClick();
        void onCollectionClick(CollectionWithCount item);
        void onDeleteClick(CollectionWithCount item);
    }

    public CollectionAdapter(OnCollectionClickListener listener) {
        this.listener = listener;
    }

    public void setCollections(List<CollectionWithCount> collections) {
        this.collectionList = (collections != null) ? collections : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_ADD_NEW;
        }
        return TYPE_COLLECTION;
    }

    @Override
    public int getItemCount() {
        return collectionList.size() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ADD_NEW) {
            ItemAddCollectionBinding binding = ItemAddCollectionBinding.inflate(inflater, parent, false);
            return new AddViewHolder(binding);
        } else {
            ItemCollectionBinding binding = ItemCollectionBinding.inflate(inflater, parent, false);
            return new CollectionViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ADD_NEW) {
            ((AddViewHolder) holder).bind();
        } else {
            CollectionWithCount item = collectionList.get(position - 1);
            ((CollectionViewHolder) holder).bind(item);
        }
    }

    class AddViewHolder extends RecyclerView.ViewHolder {
        private final ItemAddCollectionBinding binding;

        AddViewHolder(ItemAddCollectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind() {
            binding.cardAddCollection.setOnClickListener(v -> {
                if (listener != null) listener.onAddNewClick();
            });
        }
    }

    class CollectionViewHolder extends RecyclerView.ViewHolder {
        private final ItemCollectionBinding binding;

        CollectionViewHolder(ItemCollectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CollectionWithCount item) {
            binding.txtCollectionName.setText(item.collection.getName());

            if (item.collection.getDescription() != null && !item.collection.getDescription().trim().isEmpty()) {
                binding.txtCollectionDesc.setText(item.collection.getDescription());
                binding.txtCollectionDesc.setVisibility(View.VISIBLE);
            } else {
                binding.txtCollectionDesc.setVisibility(View.GONE);
            }

            int total = item.wordCount;
            int learned = item.learnedCount;
            int percent = total > 0 ? (learned * 100 / total) : 0;

            binding.progressLearning.setProgress(percent);
            binding.txtProgressPercent.setText(percent + "%");
            binding.txtWordCount.setText(learned + "/" + total + " từ đã thuộc");

            try {
                int color = Color.parseColor(item.collection.getAccentColor());
                binding.viewAccentBar.setBackgroundColor(color);
                binding.layoutIconBg.setBackgroundTintList(ColorStateList.valueOf(adjustAlpha(color, 0.2f)));
                binding.imgFolderIcon.setImageTintList(ColorStateList.valueOf(color));
            } catch (Exception e) {
                binding.viewAccentBar.setBackgroundColor(Color.parseColor("#8EC5FC"));
            }

            binding.cardCollection.setOnClickListener(v -> {
                if (listener != null) listener.onCollectionClick(item);
            });

            binding.btnOptions.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(item);
            });
        }

        private int adjustAlpha(int color, float factor) {
            int alpha = Math.round(Color.alpha(color) * factor);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            return Color.argb(alpha, red, green, blue);
        }
    }
}
