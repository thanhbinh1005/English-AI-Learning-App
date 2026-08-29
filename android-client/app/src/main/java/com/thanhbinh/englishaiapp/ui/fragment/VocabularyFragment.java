package com.thanhbinh.englishaiapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.thanhbinh.englishaiapp.CollectionDetailActivity;
import com.thanhbinh.englishaiapp.NewCollectionActivity;
import com.thanhbinh.englishaiapp.data.local.AppDatabase;
import com.thanhbinh.englishaiapp.data.model.CollectionWithCount;
import com.thanhbinh.englishaiapp.databinding.FragmentVocabularyBinding;
import com.thanhbinh.englishaiapp.ui.adapter.CollectionAdapter;

public class VocabularyFragment extends Fragment implements CollectionAdapter.OnCollectionClickListener {

    private FragmentVocabularyBinding binding;
    private CollectionAdapter adapter;

    private int totalCollectionsCount = 0;
    private int totalVocabulariesCount = 0;
    private int totalLearnedCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVocabularyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (getContext() != null) {
                AppDatabase.getInstance(requireContext().getApplicationContext()).vocabularyDao().removeDuplicates();
            }
        });
        observeData();
    }

    private void initViews() {
        adapter = new CollectionAdapter(this);
        binding.rvCollections.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvCollections.setAdapter(adapter);

        binding.fabAddCollection.setOnClickListener(v -> openNewCollectionScreen());
    }

    private void observeData() {
        AppDatabase db = AppDatabase.getInstance(requireContext());

        db.collectionDao().getCollectionsWithCount().observe(getViewLifecycleOwner(), collections -> {
            adapter.setCollections(collections);
        });

        db.collectionDao().getTotalCollectionsCount().observe(getViewLifecycleOwner(), count -> {
            totalCollectionsCount = (count != null) ? count : 0;
            binding.txtStatCollections.setText(String.valueOf(totalCollectionsCount));
        });

        db.vocabularyDao().getTotalVocabulariesCount().observe(getViewLifecycleOwner(), count -> {
            totalVocabulariesCount = (count != null) ? count : 0;
            binding.txtStatWords.setText(String.valueOf(totalVocabulariesCount));
            updateOverallProgress();
        });

        db.vocabularyDao().getTotalLearnedVocabulariesCount().observe(getViewLifecycleOwner(), count -> {
            totalLearnedCount = (count != null) ? count : 0;
            updateOverallProgress();
        });
    }

    private void updateOverallProgress() {
        int percent = (totalVocabulariesCount > 0) ? (totalLearnedCount * 100 / totalVocabulariesCount) : 0;
        binding.txtStatLearned.setText(percent + "%");
    }

    private void openNewCollectionScreen() {
        Intent intent = new Intent(requireContext(), NewCollectionActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAddNewClick() {
        openNewCollectionScreen();
    }

    @Override
    public void onCollectionClick(CollectionWithCount item) {
        Intent intent = new Intent(requireContext(), CollectionDetailActivity.class);
        intent.putExtra("collection_id", item.collection.getId());
        intent.putExtra("collection_name", item.collection.getName());
        intent.putExtra("collection_color", item.collection.getAccentColor());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(CollectionWithCount item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa bộ sưu tập")
                .setMessage("Bạn có chắc chắn muốn xóa bộ sưu tập \"" + item.collection.getName() + "\"? Tất cả từ vựng trong bộ này cũng sẽ bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        AppDatabase.getInstance(requireContext().getApplicationContext()).collectionDao().delete(item.collection);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Đã xóa bộ sưu tập", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
